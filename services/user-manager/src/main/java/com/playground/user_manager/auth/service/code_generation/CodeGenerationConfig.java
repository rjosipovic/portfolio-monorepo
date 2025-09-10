package com.playground.user_manager.auth.service.code_generation;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.code-generation")
@Configuration
@Getter
@Setter
public class CodeGenerationConfig {

    private String subject;
    private String notificationMessageTemplate;
    private String keyPrefix;
    private Duration codeExpirationTime;
}
