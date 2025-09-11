package com.playground.notification_manager.outbound.email.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.email")
@Getter @Setter
public class EmailConfig {

    private String defaultFrom;
    private String defaultTo;
}
