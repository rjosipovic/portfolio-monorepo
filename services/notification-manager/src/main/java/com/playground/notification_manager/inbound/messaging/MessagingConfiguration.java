package com.playground.notification_manager.inbound.messaging;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.messaging")
@Getter @Setter
public class MessagingConfiguration {

    private DeadLetterConfiguration deadLetter;
    private NotificationConfiguration notifications;

    @NoArgsConstructor
    @Getter @Setter
    public static class DeadLetterConfiguration {
        private String exchange;
        private String bindingKey;
        private String queue;
    }

    @NoArgsConstructor
    @Getter @Setter
    public static class NotificationConfiguration {
        private String exchange;
        private String notificationsBindingKey;
        private String queue;
    }
}
