package com.candortech.config.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {

    /** HMAC-SHA signing secret — must be at least 256 bits (32 characters). */
    private String secret;

    /** Token validity in milliseconds. Default: 24 h */
    private long expirationMs = 86400000;

    /** HTTP header that carries the token. Default: Authorization */
    private String header = "Authorization";

    /** Scheme prefix in the header value. Default: Bearer */
    private String tokenPrefix = "Bearer";
}
