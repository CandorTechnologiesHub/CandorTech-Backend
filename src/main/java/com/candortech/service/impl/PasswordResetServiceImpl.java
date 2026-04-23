package com.candortech.service.impl;

import com.candortech.entity.PasswordResetToken;
import com.candortech.entity.UserProfile;
import com.candortech.exception.ApiException;
import com.candortech.repository.PasswordResetTokenRepository;
import com.candortech.repository.UserRepository;
import com.candortech.service.EmailService;
import com.candortech.service.PasswordResetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final int tokenExpiryMinutes;
    private final String resetPasswordUrl;

    public PasswordResetServiceImpl(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder,
            @Value("${app.security.password-reset.token-expiry-minutes}") int tokenExpiryMinutes,
            @Value("${app.frontend.reset-password-url}") String resetPasswordUrl) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.tokenExpiryMinutes = tokenExpiryMinutes;
        this.resetPasswordUrl = resetPasswordUrl;
    }

    @Override
    @Transactional
    public void initiatePasswordReset(String email) {
        UserProfile user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            // Silent no-op to prevent email enumeration
            log.debug("Password reset requested for unknown email: {}", email);
            return;
        }

        tokenRepository.deleteByUser(user);

        String rawToken = UUID.randomUUID().toString();
        String tokenHash = hashToken(rawToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(tokenExpiryMinutes);

        tokenRepository.save(new PasswordResetToken(tokenHash, user, expiresAt));

        String resetLink = resetPasswordUrl + "?token=" + rawToken;
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), resetLink, tokenExpiryMinutes);
    }

    @Override
    @Transactional
    public void resetPassword(String rawToken, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new ApiException("Passwords do not match", HttpStatus.BAD_REQUEST.value());
        }

        String tokenHash = hashToken(rawToken);
        PasswordResetToken resetToken = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ApiException("Invalid reset token", HttpStatus.BAD_REQUEST.value()));

        if (resetToken.isUsed()) {
            throw new ApiException("Reset token has already been used", HttpStatus.BAD_REQUEST.value());
        }
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException("Reset token has expired", HttpStatus.BAD_REQUEST.value());
        }

        UserProfile user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }

    private String hashToken(String rawToken) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
