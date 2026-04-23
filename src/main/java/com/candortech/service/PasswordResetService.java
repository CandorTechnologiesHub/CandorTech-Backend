package com.candortech.service;

public interface PasswordResetService {

    /**
     * Initiates a password reset for the given email address.
     * Always returns successfully to prevent email enumeration.
     */
    void initiatePasswordReset(String email);

    /**
     * Validates the reset token and sets the new password.
     *
     * @throws com.candortech.exception.ApiException if the token is invalid, expired, or already used,
     *                                               or if the passwords do not match
     */
    void resetPassword(String token, String newPassword, String confirmPassword);
}
