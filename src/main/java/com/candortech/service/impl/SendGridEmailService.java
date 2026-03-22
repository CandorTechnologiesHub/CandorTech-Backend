package com.candortech.service.impl;

import com.candortech.config.MailProperties;
import com.candortech.service.EmailService;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@ConditionalOnProperty(name = "mail.service.provider", havingValue = "sendgrid")
public class SendGridEmailService implements EmailService {

    private static final String MAIL_SEND_ENDPOINT = "mail/send";

    private final SendGrid sendGrid;
    private final SpringTemplateEngine templateEngine;
    private final MailProperties mailProperties;
    private final RetryTemplate retryTemplate;

    public SendGridEmailService(
            MailProperties mailProperties,
            SpringTemplateEngine templateEngine,
            @Qualifier("emailRetryTemplate") RetryTemplate retryTemplate) {
        this.mailProperties = mailProperties;
        this.templateEngine = templateEngine;
        this.retryTemplate = retryTemplate;
        this.sendGrid = new SendGrid(mailProperties.getSendgrid().getApiKey());
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
                dispatch(toEmail, toName, mailProperties.getTemplates().getOtpSubject(), html);
                return null;
            });
            log.info("OTP email dispatched via SendGrid to {}", toEmail);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("SendGrid: all retry attempts exhausted for OTP email to {}: {}", toEmail, e.getMessage(), e);
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
                dispatch(toEmail, toName, mailProperties.getTemplates().getResetPasswordSubject(), html);
                return null;
            });
            log.info("Password-reset email dispatched via SendGrid to {}", toEmail);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("SendGrid: all retry attempts exhausted for password-reset email to {}: {}", toEmail, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    private void dispatch(String toEmail, String toName, String subject, String htmlBody) throws IOException {
        Email from = new Email(mailProperties.getFrom().getEmail(), mailProperties.getFrom().getName());
        Email to = new Email(toEmail, toName);

        Personalization personalization = new Personalization();
        personalization.addTo(to);

        Content content = new Content("text/html", htmlBody);

        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setSubject(subject);
        mail.addPersonalization(personalization);
        mail.addContent(content);

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint(MAIL_SEND_ENDPOINT);
        request.setBody(mail.build());

        Response response = sendGrid.api(request);

        if (response.getStatusCode() < 200 || response.getStatusCode() >= 300) {
            throw new IOException(
                    "SendGrid API returned non-2xx status " + response.getStatusCode()
                    + " for recipient " + toEmail + ": " + response.getBody()
            );
        }
    }
}
