package com.candortech.controller;

import com.candortech.dto.AuthResponse;
import com.candortech.dto.LoginRequest;
import com.candortech.dto.request.ForgotPasswordRequest;
import com.candortech.dto.request.ResetPasswordRequest;
import com.candortech.dto.request.UserSignupRequest;
import com.candortech.dto.response.ApiResponse;
import com.candortech.entity.UserProfile;
import com.candortech.service.AuthService;
import com.candortech.service.PasswordResetService;
import com.candortech.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final PasswordResetService passwordResetService;
    private final AuthService authService;


    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> createUserHandler(@RequestBody UserSignupRequest request) {
        return new ResponseEntity<>(
                ApiResponse.success(
                        "User Created Successfully",
                        authService.signup(request)
                ),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<AuthResponse>> signIn(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Login Successful",
                        authService.login(req)
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        authService.logout();
        return ResponseEntity.ok(ApiResponse.success("Logout Successful"));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfile> findUserByJwtToken(@RequestHeader("Authorization") String jwt) throws Exception{
        UserProfile user = userService.findUserByJwtToken(jwt);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.initiatePasswordReset(request.email());
        return ResponseEntity.ok(ApiResponse.success("If an account with that email exists, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword(), request.confirmPassword());
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully."));
    }
}