package com.playground.user_manager.messaging.callback;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "app.messaging.dead-letter")
@Configuration
@Getter
@Setter
public class DlxMessagingConfiguration {

    private String exchange;
    private String routingKey;
    private String queue;
}
