package com.candortech.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import javax.crypto.SecretKey;
import java.util.List;

@Configuration
@EnableWebSecurity
public class AppConfig {

    private final JwtProperties jwtProperties;
    private final SecretKey jwtSecretKey;

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Value("${app.cors.allowed-methods}")
    private List<String> allowedMethods;

    @Value("${app.cors.allowed-headers}")
    private List<String> allowedHeaders;

    @Value("${app.cors.max-age-seconds:3600}")
    private long corsMaxAge;

    public AppConfig(JwtProperties jwtProperties, SecretKey jwtSecretKey) {
        this.jwtProperties = jwtProperties;
        this.jwtSecretKey = jwtSecretKey;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(new JwtTokenValidator(jwtSecretKey, jwtProperties), BasicAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration cfg = new CorsConfiguration();
            cfg.setAllowedOrigins(allowedOrigins);
            cfg.setAllowedMethods(allowedMethods);
            cfg.setAllowCredentials(true);
            cfg.setAllowedHeaders(allowedHeaders);
            cfg.setExposedHeaders(List.of(jwtProperties.getHeader()));
            cfg.setMaxAge(corsMaxAge);
            return cfg;
        };
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
