package com.playground.user_manager.user.messaging;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "app.messaging.user")
@Configuration
@Getter
@Setter
public class UserMessagingConfiguration {

    private String exchange;
    private String userCreatedRoutingKey;
}
