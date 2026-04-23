package com.candortech.exception;

import org.springframework.http.HttpStatus;

public class AuthException extends ApiException {
    public AuthException(String message) {
        super(message, HttpStatus.UNAUTHORIZED.value());
    }
}
