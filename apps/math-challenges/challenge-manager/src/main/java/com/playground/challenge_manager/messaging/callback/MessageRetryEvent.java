package com.playground.challenge_manager.messaging.callback;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.springframework.amqp.core.Message;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageRetryEvent {

    Message message;
    String exchange;
    String routingKey;
}
