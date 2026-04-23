package com.candortech.dto;

import com.candortech.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private String jwt;
    private String message;
    private UserRole role;
    private Boolean isNewUser;
}