package com.candortech.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO to be used for Google SSO Login
 */
public record GoogleLoginRequest(
        @NotBlank(message = "Google ID token cannot be blank")
        String idToken
) {}
