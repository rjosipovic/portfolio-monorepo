package com.playground.user_manager.messaging.callback;

import lombok.Value;
import org.springframework.amqp.core.Message;

@Value
public class MessageRetryEvent {

    Message message;
    String exchange;
    String routingKey;
}