package com.playground.gamification_manager.auth;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.auth")
@Configuration
@Getter
@Setter
@Slf4j
public class AuthConfig {

    private String secret;
    private Duration expirationTime;

    @PostConstruct
    public void validate() {
        if (!StringUtils.hasText(secret)) {
            log.error("FATAL: JWT secret is not configured. Please set the 'app.auth.secret' property.");
            throw new IllegalStateException("Mandatory property 'app.auth.secret' is not configured.");
        }
    }
}
