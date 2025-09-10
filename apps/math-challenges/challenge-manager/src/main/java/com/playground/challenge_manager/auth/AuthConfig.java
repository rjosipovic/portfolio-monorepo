package com.playground.challenge_manager.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.auth")
@Configuration
@Getter
@Setter
public class AuthConfig {

    private String secret;
    private Duration expirationTime;
}
