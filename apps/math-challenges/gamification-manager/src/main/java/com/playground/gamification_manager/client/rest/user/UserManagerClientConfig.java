package com.playground.gamification_manager.client.rest.user;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Configuration
@ConfigurationProperties("app.clients.user-manager")
@Getter
@Setter
@Validated
public class UserManagerClientConfig {

    private String scheme = "http";
    private String host = "localhost";
    private int port = 8081;
    private String basePath = "/users";

    // Nested class for the retry properties
    private RetryConfig retry = new RetryConfig();

    @Getter
    @Setter
    public static class RetryConfig {
        private int maxAttempts = 3;
        private Duration waitDuration = Duration.ofMillis(500);
        private double exponentialBackoff = 1.5;
    }

    public String getBaseUrl() {
        return String.format("%s://%s:%d", scheme, host, port);
    }
}
