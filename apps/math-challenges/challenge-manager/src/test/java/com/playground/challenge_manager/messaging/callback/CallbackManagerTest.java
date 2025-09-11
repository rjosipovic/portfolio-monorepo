package com.playground.challenge_manager.messaging.callback;

import com.playground.challenge_manager.challenge.messaging.MessagingConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.context.ApplicationEventPublisher;

import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CallbackManagerTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private MessagingConfiguration messagingConfiguration;
    @Mock
    private PendingMessageStore pendingMessageStore;

    @InjectMocks
    private CallbackManager callbackManager;

    @Test
    void testProcessCallback_ack_removesPendingMessage() {
        //given
        var correlationId = UUID.randomUUID().toString();
        var correlationData = new CorrelationData(correlationId);
        // Simulate a pending message
        var message = mock(Message.class);
        callbackManager.put(correlationId, message);

        //when
        callbackManager.processCallback(correlationData, true);

        //then
        verify(eventPublisher, never()).publishEvent(any());
        verify(pendingMessageStore).delete(correlationId);
    }

    @Test
    void testProcessCallback_nack_publishesRetryEvent() {
        //given
        var correlationId = UUID.randomUUID().toString();
        var correlationData = new CorrelationData(correlationId);
        var message = mock(Message.class);
        var messageProperties = mock(MessageProperties.class);
        var headers = new HashMap<String, Object>();
        headers.put("x-retry-count", 1);
        headers.put("x-exchange", "exchange");
        headers.put("x-routing-key", "routingKey");
        callbackManager.put(correlationId, message);

        when(message.getMessageProperties()).thenReturn(messageProperties);
        when(messageProperties.getHeaders()).thenReturn(headers);
        when(pendingMessageStore.get(correlationId)).thenReturn(message);

        //when
        callbackManager.processCallback(correlationData, false);

        //then
        var captor = ArgumentCaptor.forClass(MessageRetryEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        var event = captor.getValue();

        assertAll(
                () -> assertNotNull(event),
                () -> assertEquals(message, event.getMessage()),
                () -> assertEquals("exchange", event.getExchange()),
                () -> assertEquals("routingKey", event.getRoutingKey())
        );
    }
    @Test
    void testProcessCallback_nack_publishesRetryEvent_toDLX() {
        //given
        var correlationId = UUID.randomUUID().toString();
        var correlationData = new CorrelationData(correlationId);
        var message = mock(Message.class);
        var messageProperties = mock(MessageProperties.class);
        var headers = new HashMap<String, Object>();
        var dlxConfig = mock(MessagingConfiguration.DeadLetterConfiguration.class);
        headers.put("x-retry-count", 3);
        headers.put("x-exchange", "exchange");
        headers.put("x-routing-key", "routingKey");
        callbackManager.put(correlationId, message);

        when(message.getMessageProperties()).thenReturn(messageProperties);
        when(messageProperties.getHeaders()).thenReturn(headers);
        when(pendingMessageStore.get(correlationId)).thenReturn(message);

        when(messagingConfiguration.getDeadLetter()).thenReturn(dlxConfig);
        when(dlxConfig.getExchange()).thenReturn("dlx-exchange");
        when(dlxConfig.getRoutingKey()).thenReturn("dlx-routing-key");

        //when
        callbackManager.processCallback(correlationData, false);

        //then
        var captor = ArgumentCaptor.forClass(MessageRetryEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        var event = captor.getValue();

        assertAll(
                () -> assertNotNull(event),
                () -> assertEquals(message, event.getMessage()),
                () -> assertEquals("dlx-exchange", event.getExchange()),
                () -> assertEquals("dlx-routing-key", event.getRoutingKey())
        );
    }
}