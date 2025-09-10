package com.playground.user_manager.auth.messaging.producers;

import com.playground.user_manager.auth.messaging.AuthMessagingConfiguration;
import com.playground.user_manager.auth.messaging.AuthNotification;
import com.playground.user_manager.messaging.callback.CallbackManager;
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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthMessageProducerTest {

    @Mock
    private AuthMessagingConfiguration authMessagingConfiguration;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private CallbackManager callbackManager;
    @InjectMocks
    private AuthMessageProducer producer;


    @Test
    void sendMessage_sendsToCorrectQueueWithCorrectPayload() {
        // given
        var to = "user@example.com";
        var subject = "code";
        var body = "123456";
        var notification = AuthNotification.builder().to(to).subject(subject).body(body).build();
        var exchange = "notification-exchange";
        var routingKey = "notifications";
        when(authMessagingConfiguration.getExchange()).thenReturn(exchange);
        when(authMessagingConfiguration.getAuthCodeRoutingKey()).thenReturn(routingKey);
        var messageConverter = mock(MessageConverter.class);
        when(rabbitTemplate.getMessageConverter()).thenReturn(messageConverter);
        when(messageConverter.toMessage(eq(notification), any(MessageProperties.class))).thenReturn(mock(Message.class));


        // when
        producer.sendAuthCode(notification);

        // then
        var exchangeCaptor = ArgumentCaptor.forClass(String.class);
        var routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        var messageCaptor = ArgumentCaptor.forClass(Message.class);
        var correlationCaptor = ArgumentCaptor.forClass(CorrelationData.class);

        verify(rabbitTemplate).convertAndSend(
                exchangeCaptor.capture(),
                routingKeyCaptor.capture(),
                messageCaptor.capture(),
                correlationCaptor.capture()
        );
        assertAll(
                () -> assertEquals(exchange, exchangeCaptor.getValue()),
                () -> assertEquals(routingKey, routingKeyCaptor.getValue())
        );
        verify(callbackManager, times(1)).put(any(String.class), any(Message.class));
    }
}