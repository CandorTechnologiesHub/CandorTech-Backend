package com.candortech.config.security;

import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
public class JwtConfig {

    @Bean
    public SecretKey jwtSecretKey(JwtProperties jwtProperties) {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }
}
