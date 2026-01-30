package com.playground.challenge_manager.challenge.messaging.producers;

import com.playground.challenge_manager.challenge.messaging.MessagingConfiguration;
import com.playground.challenge_manager.challenge.messaging.events.ChallengeSolvedEvent;
import com.playground.challenge_manager.messaging.callback.CallbackManager;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ChallengeAttemptProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private MessagingConfiguration messagingConfiguration;

    @Mock
    private CallbackManager callbackManager;

    @Mock
    private MessagingConfiguration.ChallengeConfiguration challengeConfiguration;

    @InjectMocks
    private ChallengeAttemptProducer producer;

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldSendToCorrectRoutingKeyWhenCorrect() {
        // given
        var exchange = "challenge-exchange";
        var correctRoutingKey = "challenge.correct";
        var failedRoutingKey = "challenge.failed";
        var dto = mock(ChallengeSolvedEvent.class);
        when(dto.isCorrect()).thenReturn(true);
        when(dto.getChallengeAttemptId()).thenReturn(UUID.randomUUID().toString());

        when(messagingConfiguration.getChallenge()).thenReturn(challengeConfiguration);
        when(challengeConfiguration.getExchange()).thenReturn(exchange);
        when(challengeConfiguration.getChallengeCorrectRoutingKey()).thenReturn(correctRoutingKey);
        when(challengeConfiguration.getChallengeFailedRoutingKey()).thenReturn(failedRoutingKey);

        var messageConverter = mock(MessageConverter.class);
        when(rabbitTemplate.getMessageConverter()).thenReturn(messageConverter);
        when(messageConverter.toMessage(eq(dto), any(MessageProperties.class))).thenReturn(mock(Message.class));

        // when
        producer.publishChallengeSolvedMessage(dto);

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
                () -> assertEquals(correctRoutingKey, routingKeyCaptor.getValue())
        );
        verify(callbackManager).put(correlationCaptor.getValue().getId(), messageCaptor.getValue());
        assertAll(
                () -> assertEquals(dto.getChallengeAttemptId(), correlationCaptor.getValue().getId())
        );
    }

    @Test
    void shouldSendToFailedRoutingKeyWhenIncorrect() {
        // given
        var exchange = "challenge-exchange";
        var correctRoutingKey = "challenge.correct";
        var failedRoutingKey = "challenge.failed";
        var dto = mock(ChallengeSolvedEvent.class);
        when(dto.isCorrect()).thenReturn(false);
        when(dto.getChallengeAttemptId()).thenReturn(UUID.randomUUID().toString());

        when(messagingConfiguration.getChallenge()).thenReturn(challengeConfiguration);
        when(challengeConfiguration.getExchange()).thenReturn(exchange);
        when(challengeConfiguration.getChallengeCorrectRoutingKey()).thenReturn(correctRoutingKey);
        when(challengeConfiguration.getChallengeFailedRoutingKey()).thenReturn(failedRoutingKey);

        var messageConverter = mock(MessageConverter.class);
        when(rabbitTemplate.getMessageConverter()).thenReturn(messageConverter);
        when(messageConverter.toMessage(eq(dto), any(MessageProperties.class))).thenReturn(mock(Message.class));

        // when
        producer.publishChallengeSolvedMessage(dto);

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
                () -> assertEquals(failedRoutingKey, routingKeyCaptor.getValue())
        );
        verify(callbackManager).put(correlationCaptor.getValue().getId(), messageCaptor.getValue());
        assertAll(
                () -> assertEquals(dto.getChallengeAttemptId(), correlationCaptor.getValue().getId())
        );
    }
}