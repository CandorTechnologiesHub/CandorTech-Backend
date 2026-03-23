package com.candortech.service.impl;

import com.candortech.config.security.JwtProvider;
import com.candortech.dto.AuthResponse;
import com.candortech.dto.LoginRequest;
import com.candortech.dto.request.UserSignupRequest;
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

import java.security.SecureRandom;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final JwtProvider jwtProvider;
    private final CustomUserDetails customUserDetails;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final EmailService emailService;

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
                userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtProvider.generateToken(authentication);

        // Send registration email asynchronously
        String registrationOtp = generate6DigitOtp();
        emailService.sendOtpEmail(
                savedUser.getEmail(),
                savedUser.getFirstName(),
                registrationOtp,
                10,
                OtpPurpose.REGISTRATION
        );

        return AuthResponse.builder()
                .jwt(jwt)
                .role(savedUser.getRole())
                .message("Register success")
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticate(request.getEmail(), request.getPassword());

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String role = authorities.isEmpty() ? null : authorities.iterator().next().getAuthority();

        String jwt = jwtProvider.generateToken(authentication);

        return AuthResponse.builder()
                .jwt(jwt)
                .role(role != null ? USER_ROLE.valueOf(role) : null)
                .message("Login success")
                .build();
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
                userDetails.getAuthorities()
        );
    }

    private String generate6DigitOtp() {
        SecureRandom secureRandom = new SecureRandom();
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }
}