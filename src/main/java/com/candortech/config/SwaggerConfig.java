package com.candortech.config;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("candortech")
                .pathsToMatch("/api/**/**", "/api/**/**")
                .build();
    }

    @Bean
    public io.swagger.v3.oas.models.OpenAPI customOpenAPI() {
        return new io.swagger.v3.oas.models.OpenAPI()
                .info(new Info()
                        .title("candortech")
                        .version("1.0")
                        .description("API documentation for the candortech")
                        .contact(new Contact().name("Innocent Udo").email("innocentcharlesudo@gmail.com"))
                );
    }
}