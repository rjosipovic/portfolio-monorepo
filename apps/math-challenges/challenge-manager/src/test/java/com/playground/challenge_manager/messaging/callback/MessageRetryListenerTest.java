package com.playground.challenge_manager.messaging.callback;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MessageRetryListenerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;
    @InjectMocks
    private MessageRetryListener listener;

    @Test
    void handleRetry_shouldSendMessageWithCorrectArguments() {
        // given
        var exchange = "test-exchange";
        var routingKey = "test-routing-key";
        var message = mock(Message.class);
        var event = MessageRetryEvent.builder().message(message).exchange(exchange).routingKey(routingKey).build();

        // when
        listener.onMessageRetryEvent(event);

        // then
        var messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(rabbitTemplate).send(eq(exchange), eq(routingKey), messageCaptor.capture());
        assertAll(
                () -> assertEquals(message, messageCaptor.getValue()),
                () -> assertEquals(exchange, event.getExchange()),
                () -> assertEquals(routingKey, event.getRoutingKey())
        );
    }
}