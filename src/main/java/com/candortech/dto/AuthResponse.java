package com.candortech.dto;

import com.candortech.enums.USER_ROLE;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private String jwt;
    private String message;
    private USER_ROLE role;
    private Boolean isNewUser;
}