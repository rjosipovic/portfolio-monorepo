package com.playground.notification_manager.inbound.messaging.auth;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = AuthNotification.AuthNotificationBuilder.class)
public class AuthNotification {

    String to;
    String subject;
    String body;

    @JsonPOJOBuilder(withPrefix = "")
    public static class AuthNotificationBuilder {
    }
}
