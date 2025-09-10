package com.playground.analytics_manager.inbound.messaging;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "app.messaging")
@Configuration
@Getter
@Setter
public class MessagingConfiguration {

    private DlqMessagingConfiguration deadLetter;
    private UserMessagingConfiguration user;
    private ChallengeMessagingConfiguration challenge;

    @NoArgsConstructor
    @Getter @Setter
    public static class DlqMessagingConfiguration {

        private String exchange;
        private String bindingKey;
        private String queue;
    }

    @NoArgsConstructor
    @Getter @Setter
    public static class UserMessagingConfiguration {

        private String exchange;
        private String bindingKey;
        private String queue;
    }

    @NoArgsConstructor
    @Getter @Setter
    public static class ChallengeMessagingConfiguration {

        private String exchange;
        private String bindingKey;
        private String queue;
    }
}
