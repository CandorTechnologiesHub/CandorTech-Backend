package com.candortech.service.impl;

import com.candortech.entity.PasswordResetToken;
import com.candortech.entity.UserProfile;
import com.candortech.exception.ApiException;
import com.candortech.repository.PasswordResetTokenRepository;
import com.candortech.repository.UserRepository;
import com.candortech.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordResetTokenRepository tokenRepository;
    @Mock private EmailService emailService;
    @Mock private PasswordEncoder passwordEncoder;

    private PasswordResetServiceImpl service;

    private static final int EXPIRY_MINUTES = 30;
    private static final String RESET_URL = "http://localhost:3000/reset-password";

    @BeforeEach
    void setUp() {
        service = new PasswordResetServiceImpl(
                userRepository, tokenRepository, emailService, passwordEncoder,
                EXPIRY_MINUTES, RESET_URL);
    }

    // -------------------------------------------------------------------------
    // initiatePasswordReset
    // -------------------------------------------------------------------------

    @Test
    void initiatePasswordReset_unknownEmail_doesNothing() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(null);

        service.initiatePasswordReset("nobody@example.com");

        verifyNoInteractions(tokenRepository, emailService);
    }

    @Test
    void initiatePasswordReset_knownEmail_deletesOldTokensAndSavesNew() {
        UserProfile user = buildUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);

        service.initiatePasswordReset(user.getEmail());

        verify(tokenRepository).deleteByUser(user);

        ArgumentCaptor<PasswordResetToken> saved = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(saved.capture());

        PasswordResetToken token = saved.getValue();
        assertThat(token.getUser()).isEqualTo(user);
        assertThat(token.getTokenHash()).isNotBlank();
        assertThat(token.getExpiresAt()).isAfter(LocalDateTime.now());
        assertThat(token.isUsed()).isFalse();
    }

    @Test
    void initiatePasswordReset_knownEmail_sendsResetEmailWithCorrectArgs() {
        UserProfile user = buildUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);

        service.initiatePasswordReset(user.getEmail());

        ArgumentCaptor<String> resetLinkCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendPasswordResetEmail(
                eq(user.getEmail()),
                eq(user.getFullName()),
                resetLinkCaptor.capture(),
                eq(EXPIRY_MINUTES));

        String resetLink = resetLinkCaptor.getValue();
        assertThat(resetLink).startsWith(RESET_URL + "?token=");
    }

    @Test
    void initiatePasswordReset_tokenHash_matchesRawTokenSha256() {
        UserProfile user = buildUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);

        service.initiatePasswordReset(user.getEmail());

        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendPasswordResetEmail(any(), any(), linkCaptor.capture(), anyInt());
        String rawToken = linkCaptor.getValue().substring((RESET_URL + "?token=").length());

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        String storedHash = tokenCaptor.getValue().getTokenHash();

        assertThat(storedHash).isEqualTo(sha256(rawToken));
    }

    // -------------------------------------------------------------------------
    // resetPassword
    // -------------------------------------------------------------------------

    @Test
    void resetPassword_passwordsMismatch_throwsApiException() {
        assertThatThrownBy(() -> service.resetPassword("token", "newPass1!", "differentPass!"))
                .isInstanceOf(ApiException.class)
                .hasMessage("Passwords do not match");
    }

    @Test
    void resetPassword_tokenNotFound_throwsApiException() {
        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resetPassword("unknown-token", "newPass1!", "newPass1!"))
                .isInstanceOf(ApiException.class)
                .hasMessage("Invalid reset token");
    }

    @Test
    void resetPassword_tokenAlreadyUsed_throwsApiException() {
        PasswordResetToken token = buildToken(true, LocalDateTime.now().plusMinutes(10));
        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword("any-token", "newPass1!", "newPass1!"))
                .isInstanceOf(ApiException.class)
                .hasMessage("Reset token has already been used");
    }

    @Test
    void resetPassword_tokenExpired_throwsApiException() {
        PasswordResetToken token = buildToken(false, LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword("any-token", "newPass1!", "newPass1!"))
                .isInstanceOf(ApiException.class)
                .hasMessage("Reset token has expired");
    }

    @Test
    void resetPassword_validToken_updatesPasswordAndMarksTokenUsed() {
        UserProfile user = buildUser();
        PasswordResetToken token = buildToken(false, LocalDateTime.now().plusMinutes(10));
        token.setUser(user);

        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newPass1!")).thenReturn("encoded-password");

        service.resetPassword("valid-token", "newPass1!", "newPass1!");

        assertThat(user.getPassword()).isEqualTo("encoded-password");
        assertThat(token.isUsed()).isTrue();
        verify(userRepository).save(user);
        verify(tokenRepository).save(token);
    }

    @Test
    void resetPassword_validToken_doesNotSendEmail() {
        UserProfile user = buildUser();
        PasswordResetToken token = buildToken(false, LocalDateTime.now().plusMinutes(10));
        token.setUser(user);

        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        service.resetPassword("valid-token", "newPass1!", "newPass1!");

        verifyNoInteractions(emailService);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private UserProfile buildUser() {
        UserProfile user = new UserProfile();
        user.setEmail("alice@example.com");
        user.setFullName("Alice");
        user.setPassword("old-encoded-password");
        return user;
    }

    private PasswordResetToken buildToken(boolean used, LocalDateTime expiresAt) {
        PasswordResetToken token = new PasswordResetToken("hash", buildUser(), expiresAt);
        token.setUsed(used);
        return token;
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
