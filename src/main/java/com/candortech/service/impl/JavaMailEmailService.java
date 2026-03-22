package com.candortech.service.impl;

import com.candortech.config.MailProperties;
import com.candortech.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@ConditionalOnProperty(name = "mail.service.provider", havingValue = "javamail", matchIfMissing = true)
public class JavaMailEmailService implements EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final MailProperties mailProperties;
    private final RetryTemplate retryTemplate;

    public JavaMailEmailService(
            JavaMailSender mailSender,
            SpringTemplateEngine templateEngine,
            MailProperties mailProperties,
            @Qualifier("emailRetryTemplate") RetryTemplate retryTemplate) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.mailProperties = mailProperties;
        this.retryTemplate = retryTemplate;
    }

    @Async
    @Override
    public CompletableFuture<Void> sendOtpEmail(String toEmail, String toName, String otpCode, int expiryMinutes) {
        try {
            retryTemplate.execute(ctx -> {
                Context thymeleafCtx = new Context();
                thymeleafCtx.setVariable("name", toName);
                thymeleafCtx.setVariable("otpCode", otpCode);
                thymeleafCtx.setVariable("expiryMinutes", expiryMinutes);
                String html = templateEngine.process("email/otp", thymeleafCtx);
                sendHtmlEmail(toEmail, mailProperties.getTemplates().getOtpSubject(), html);
                return null;
            });
            log.info("OTP email dispatched via JavaMail to {}", toEmail);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("JavaMail: all retry attempts exhausted for OTP email to {}: {}", toEmail, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async
    @Override
    public CompletableFuture<Void> sendPasswordResetEmail(String toEmail, String toName, String resetLink, int expiryMinutes) {
        try {
            retryTemplate.execute(ctx -> {
                Context thymeleafCtx = new Context();
                thymeleafCtx.setVariable("name", toName);
                thymeleafCtx.setVariable("resetLink", resetLink);
                thymeleafCtx.setVariable("expiryMinutes", expiryMinutes);
                String html = templateEngine.process("email/reset-password", thymeleafCtx);
                sendHtmlEmail(toEmail, mailProperties.getTemplates().getResetPasswordSubject(), html);
                return null;
            });
            log.info("Password-reset email dispatched via JavaMail to {}", toEmail);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("JavaMail: all retry attempts exhausted for password-reset email to {}: {}", toEmail, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlBody)
            throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name()
        );
        helper.setFrom(mailProperties.getFrom().getEmail(), mailProperties.getFrom().getName());
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(message);
    }
}
