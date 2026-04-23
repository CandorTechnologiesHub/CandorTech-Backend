package com.candortech.config;

import com.candortech.enums.OtpPurpose;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "mail")
public class MailProperties {

    private Service service = new Service();
    private From from = new From();
    private Sendgrid sendgrid = new Sendgrid();
    private Retry retry = new Retry();
    private Templates templates = new Templates();

    @Data
    public static class Service {
        /** Accepted values: javamail | sendgrid */
        private String provider = "javamail";
    }

    @Data
    public static class From {
        private String email = "noreply@candortech.com";
        private String name = "CanDor";
    }

    @Data
    public static class Sendgrid {
        private String apiKey;
    }

    @Data
    public static class Templates {
        private String otpRegistrationSubject    = "Verify your CanDor account";
        private String otpLoginMfaSubject        = "Your CanDor login code";
        private String otpPasswordResetSubject   = "Your CanDor password reset code";
        private String otpPhoneChangeSubject     = "Verify your new phone number";
        private String otpEmailChangeSubject     = "Verify your new email address";
        private String resetPasswordSubject      = "Reset Your CanDor Password";

        public String resolveOtpSubject(OtpPurpose purpose) {
            return switch (purpose) {
                case REGISTRATION   -> otpRegistrationSubject;
                case LOGIN_MFA      -> otpLoginMfaSubject;
                case PASSWORD_RESET -> otpPasswordResetSubject;
                case PHONE_CHANGE   -> otpPhoneChangeSubject;
                case EMAIL_CHANGE   -> otpEmailChangeSubject;
            };
        }
    }

    @Data
    public static class Retry {
        /** Total number of attempts (1 = no retry). */
        private int maxAttempts = 3;
        /** Initial backoff delay in milliseconds. */
        private long initialIntervalMs = 1000;
        /** Backoff multiplier applied after each failed attempt. */
        private double multiplier = 2.0;
        /** Upper bound for the backoff delay in milliseconds. */
        private long maxIntervalMs = 10000;
    }
}
