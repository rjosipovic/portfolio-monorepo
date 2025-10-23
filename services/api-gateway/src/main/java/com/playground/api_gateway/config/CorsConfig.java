package com.playground.api_gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "app.cors")
@Configuration
@Getter
@Setter
public class CorsConfig {

    private String[] allowedOrigins;
    private String[] allowedMethods;
    private String[] allowedHeaders;
}