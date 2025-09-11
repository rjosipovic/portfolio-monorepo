package com.playground.user_manager.auth.messaging;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "app.messaging.auth")
@Configuration
@Getter @Setter
public class AuthMessagingConfiguration {

    private String exchange;
    private String authCodeRoutingKey;
}
