package com.playground.user_manager.auth.messaging;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthNotification {

    String to;
    String subject;
    String body;
}
