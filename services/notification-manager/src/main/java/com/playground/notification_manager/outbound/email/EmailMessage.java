package com.playground.notification_manager.outbound.email;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EmailMessage {

    String from;
    String to;
    String subject;
    String body;
}
