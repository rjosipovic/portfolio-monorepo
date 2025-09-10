package com.playground.user_manager.user.producers;

import com.playground.user_manager.messaging.callback.CallbackManager;
import com.playground.user_manager.user.messaging.UserLifecycleEvent;
import com.playground.user_manager.user.messaging.UserMessagingConfiguration;
import com.playground.user_manager.user.messaging.producers.UserMessageProducer;
import com.playground.user_manager.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserMessageProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private UserMessagingConfiguration userMessageConfiguration;

    @Mock
    private CallbackManager callbackManager;

    @InjectMocks
    private UserMessageProducer userMessageProducer;

    @Test
    void shouldSendUserCreatedMessage() {
        // given
        var userId = UUID.randomUUID().toString();
        var alias = "alias";
        var user = User.builder().id(userId).alias(alias).build();
        var exchange = "user.exchange";
        var routingKey = "user.created";
        when(userMessageConfiguration.getExchange()).thenReturn(exchange);
        when(userMessageConfiguration.getUserCreatedRoutingKey()).thenReturn(routingKey);

        var messageConverter = mock(MessageConverter.class);
        when(rabbitTemplate.getMessageConverter()).thenReturn(messageConverter);
        when(messageConverter.toMessage(any(UserLifecycleEvent.class), any(MessageProperties.class))).thenReturn(mock(Message.class));

        // when
        userMessageProducer.sendUserCreatedMessage(user);

        // then
        var exchangeCaptor = ArgumentCaptor.forClass(String.class);
        var routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        var messageCaptor = ArgumentCaptor.forClass(Message.class);
        var correlationCaptor = ArgumentCaptor.forClass(CorrelationData.class);
        verify(rabbitTemplate).convertAndSend(
                exchangeCaptor.capture(),
                routingKeyCaptor.capture(),
                messageCaptor.capture(),
                correlationCaptor.capture());

        assertAll(
                () -> assertEquals(exchange, exchangeCaptor.getValue()),
                () -> assertEquals(routingKey, routingKeyCaptor.getValue())
        );
        verify(callbackManager, times(1)).put(any(String.class), any(Message.class));
    }
}