package com.playground.user_manager.auth.messaging.producers;

import com.playground.user_manager.auth.messaging.AuthMessagingConfiguration;
import com.playground.user_manager.auth.messaging.AuthNotification;
import com.playground.user_manager.messaging.callback.CallbackManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthMessageProducer {

    private final CallbackManager callbackManager;
    private final RabbitTemplate rabbitTemplate;
    private final AuthMessagingConfiguration authMessagingConfiguration;

    public void sendAuthCode(AuthNotification authNotification) {
        log.info("Sending auth code to {}", authNotification.getTo());
        var exchange = authMessagingConfiguration.getExchange();
        var routingKey = authMessagingConfiguration.getAuthCodeRoutingKey();
        sendMessage(exchange, routingKey, authNotification);
    }

    private void sendMessage(String exchange, String routingKey, AuthNotification authNotification) {
        var correlationId = UUID.randomUUID().toString();
        var correlationData = new CorrelationData(correlationId);
        var messageProperties = MessagePropertiesBuilder.newInstance()
                .setHeader("x-retry-count", 0)
                .setHeader("x-exchange", exchange)
                .setHeader("x-routing-key", routingKey)
                .build();

        var message = buildMessage(authNotification, messageProperties);
        callbackManager.put(correlationData.getId(), message);
        rabbitTemplate.convertAndSend(exchange, routingKey, message, correlationData);
    }

    private Message buildMessage(AuthNotification authNotification, MessageProperties messageProperties) {
        return rabbitTemplate.getMessageConverter().toMessage(authNotification, messageProperties);
    }
}
