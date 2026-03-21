package com.candortech.service;

import java.util.concurrent.CompletableFuture;

/**
 * Contract for sending transactional emails.
 * Implementations are selected at startup via {@code mail.service.provider}.
 * All methods execute asynchronously on the configured thread pool executor.
 */
public interface EmailService {

    /**
     * Send a one-time password (OTP) email to the given recipient.
     *
     * @param toEmail       recipient email address
     * @param toName        recipient display name
     * @param otpCode       the 6-digit OTP to include in the email
     * @param expiryMinutes how many minutes until the OTP expires
     */
    CompletableFuture<Void> sendOtpEmail(String toEmail, String toName, String otpCode, int expiryMinutes);

    /**
     * Send a password-reset email containing a secure reset link.
     *
     * @param toEmail       recipient email address
     * @param toName        recipient display name
     * @param resetLink     the full password-reset URL
     * @param expiryMinutes how many minutes until the reset link expires
     */
    CompletableFuture<Void> sendPasswordResetEmail(String toEmail, String toName, String resetLink, int expiryMinutes);
}
