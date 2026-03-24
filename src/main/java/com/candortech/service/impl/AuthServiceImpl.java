package com.candortech.service.impl;

import com.candortech.config.security.JwtProvider;
import com.candortech.dto.AuthResponse;
import com.candortech.dto.LoginRequest;
import com.candortech.dto.request.UserSignupRequest;
import com.candortech.dto.request.GoogleLoginRequest;
import com.candortech.entity.UserProfile;
import com.candortech.enums.OtpPurpose;
import com.candortech.enums.USER_ROLE;
import com.candortech.repository.UserRepository;
import com.candortech.service.AuthService;
import com.candortech.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.candortech.exception.AuthException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final JwtProvider jwtProvider;
    private final CustomUserDetails customUserDetails;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    @Override
    public AuthResponse signup(UserSignupRequest request) {
        UserProfile existingUser = userRepository.findByEmail(request.email());
        if (existingUser != null) {
            throw new IllegalArgumentException("Email is already used with another account");
        }

        UserProfile user = new UserProfile();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setOtherNames(request.otherNames());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setPassword(passwordEncoder.encode(request.password()));

        UserProfile savedUser = userRepository.save(user);

        UserDetails userDetails = customUserDetails.loadUserByUsername(savedUser.getEmail());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtProvider.generateToken(authentication);

        // Send registration email asynchronously
        String registrationOtp = generate6DigitOtp();
        emailService.sendOtpEmail(
                savedUser.getEmail(),
                savedUser.getFirstName(),
                registrationOtp,
                10,
                OtpPurpose.REGISTRATION);

        return AuthResponse.builder()
                .jwt(jwt)
                .role(savedUser.getRole())
                .message("Register success")
                .isNewUser(true)
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        UserProfile user = userRepository.findByEmail(request.getEmail());
        if (user != null && user.isOAuthAccount()) {
            throw new AuthException(
                    "Password login is disabled for Google-registered accounts. Please use Google Login.");
        }

        Authentication authentication = authenticate(request.getEmail(), request.getPassword());

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String role = authorities.isEmpty() ? null : authorities.iterator().next().getAuthority();

        String jwt = jwtProvider.generateToken(authentication);

        return AuthResponse.builder()
                .jwt(jwt)
                .role(role != null ? USER_ROLE.valueOf(role) : null)
                .message("Login success")
                .isNewUser(false)
                .build();
    }

    @Override
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        GoogleIdToken idToken;
        try {
            idToken = googleIdTokenVerifier.verify(request.idToken());
        } catch (Exception e) {
            throw new AuthException("Google token verification failed: " + e.getMessage());
        }

        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();

            if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
                throw new AuthException("Google account email is not verified");
            }

            String email = payload.getEmail();
            String familyName = (String) payload.get("family_name");
            String givenName = (String) payload.get("given_name");

            UserProfile user = userRepository.findByEmail(email);
            boolean isNewUser = false;

            if (user == null) {
                isNewUser = true;
                user = new UserProfile();
                user.setEmail(email);
                user.setFirstName(givenName != null ? givenName : "GoogleUser");
                user.setLastName(familyName != null ? familyName : "");
                user.setOAuthAccount(true);
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                user = userRepository.save(user);
            }

            UserDetails userDetails = customUserDetails.loadUserByUsername(user.getEmail());
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtProvider.generateToken(authentication);

            return AuthResponse.builder()
                    .jwt(jwt)
                    .role(user.getRole())
                    .message("Google login success")
                    .isNewUser(isNewUser)
                    .build();

        } else {
            throw new AuthException("Invalid Google ID token.");
        }
    }

    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
    }

    private Authentication authenticate(String username, String password) {
        UserDetails userDetails = customUserDetails.loadUserByUsername(username);

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid password....");
        }

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());
    }

    private String generate6DigitOtp() {
        SecureRandom secureRandom = new SecureRandom();
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }
}