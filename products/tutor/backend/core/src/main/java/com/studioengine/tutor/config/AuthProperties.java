package com.studioengine.tutor.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Objects;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    private String jwtSecret;
    private Duration jwtExpiration;
    private Duration otpExpiration;
    private int otpLength;
    private int otpMaxAttempts;
    private Duration otpLockoutWindow;
    private String instructorEmail;

    @PostConstruct
    void validate() {
        if (Objects.isNull(jwtSecret) || jwtSecret.isBlank()) {
            throw new IllegalStateException("app.auth.jwt-secret must not be blank");
        }
        if (Objects.isNull(instructorEmail) || instructorEmail.isBlank()) {
            throw new IllegalStateException("app.auth.instructor-email must not be blank");
        }
    }
}
