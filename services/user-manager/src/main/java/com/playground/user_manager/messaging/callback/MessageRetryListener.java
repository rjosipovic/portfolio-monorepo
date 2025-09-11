package com.playground.user_manager.messaging.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessageRetryListener {

    private final RabbitTemplate rabbitTemplate;

    @EventListener
    public void onMessageRetryEvent(MessageRetryEvent event) {
        log.info("Retrying message: {}", event.getMessage());
        var message = event.getMessage();
        var exchange = event.getExchange();
        var routingKey = event.getRoutingKey();
        rabbitTemplate.send(exchange, routingKey, message);
    }
}
