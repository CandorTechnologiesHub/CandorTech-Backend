package com.candortech.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailException;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;

import java.io.IOException;

@Slf4j
@Configuration
public class EmailConfig {

    /**
     * Shared RetryTemplate for all email providers.
     * Uses exponential backoff; retries on transient mail/network failures.
     * Qualifier: "emailRetryTemplate" — avoids collisions with future Resilience4j retry beans.
     */
    @Bean("emailRetryTemplate")
    public RetryTemplate emailRetryTemplate(MailProperties mailProperties) {
        MailProperties.Retry cfg = mailProperties.getRetry();

        return RetryTemplate.builder()
                .maxAttempts(cfg.getMaxAttempts())
                .exponentialBackoff(cfg.getInitialIntervalMs(), cfg.getMultiplier(), cfg.getMaxIntervalMs())
                .retryOn(MailException.class)
                .retryOn(IOException.class)
                .withListener(new RetryListener() {
                    @Override
                    public <T, E extends Throwable> void onError(
                            RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                        log.warn(
                                "Email send attempt {}/{} failed: {}",
                                context.getRetryCount(),
                                cfg.getMaxAttempts(),
                                throwable.getMessage()
                        );
                    }
                })
                .build();
    }
}
