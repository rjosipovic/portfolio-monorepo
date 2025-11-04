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

    private String serviceName;

    private RetryConfig retry;

    @Getter
    @Setter
    public static class RetryConfig {
        private int maxAttempts;
        private Duration waitDuration;
        private double exponentialBackoff;
    }
}
