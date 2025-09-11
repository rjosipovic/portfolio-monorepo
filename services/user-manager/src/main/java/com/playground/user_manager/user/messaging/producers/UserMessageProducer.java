package com.playground.user_manager.user.messaging.producers;

import com.playground.user_manager.messaging.callback.CallbackManager;
import com.playground.user_manager.user.messaging.LifecycleType;
import com.playground.user_manager.user.messaging.UserLifecycleEvent;
import com.playground.user_manager.user.messaging.UserMessagingConfiguration;
import com.playground.user_manager.user.model.User;
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
public class UserMessageProducer {

    private final CallbackManager callbackManager;
    private final RabbitTemplate rabbitTemplate;
    private final UserMessagingConfiguration userMessagingConfiguration;

    public void sendUserCreatedMessage(User user) {
        log.info("About to publish user created message: {}", user);
        var exchange = userMessagingConfiguration.getExchange();
        var routingKey = userMessagingConfiguration.getUserCreatedRoutingKey();
        var lifecycleEvent = UserLifecycleEvent.builder().user(user).lifecycleType(LifecycleType.CREATED).build();
        sendMessage(exchange, routingKey, lifecycleEvent);
    }

    private void sendMessage(String exchange, String routingKey, UserLifecycleEvent userEvent) {
        var correlationId = UUID.randomUUID().toString();
        var correlationData = new CorrelationData(correlationId);
        var messageProperties = MessagePropertiesBuilder.newInstance()
                .setHeader("x-retry-count", 0)
                .setHeader("x-exchange", exchange)
                .setHeader("x-routing-key", routingKey)
                .build();
        var message = buildMessage(userEvent, messageProperties);
        callbackManager.put(correlationData.getId(), message);
        rabbitTemplate.convertAndSend(exchange, routingKey, message, correlationData);
    }

    private Message buildMessage(UserLifecycleEvent userEvent, MessageProperties messageProperties) {
        return rabbitTemplate.getMessageConverter().toMessage(userEvent, messageProperties);
    }
}
