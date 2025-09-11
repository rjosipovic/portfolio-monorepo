package com.playground.challenge_manager.challenge.messaging.producers;

import com.playground.challenge_manager.challenge.messaging.MessagingConfiguration;
import com.playground.challenge_manager.challenge.messaging.events.ChallengeSolvedEvent;
import com.playground.challenge_manager.messaging.callback.CallbackManager;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChallengeSolvedProducer {

    private final RabbitTemplate rabbitTemplate;
    private final MessagingConfiguration messagingConfiguration;
    private final CallbackManager callbackManager;

    public void publishChallengeSolvedMessage(ChallengeSolvedEvent challengeSolvedDTO) {
        var challengeConfiguration = messagingConfiguration.getChallenge();
        var exchange = challengeConfiguration.getExchange();
        var correctRoutingKey = challengeConfiguration.getChallengeCorrectRoutingKey();
        var incorrectRoutingKey = challengeConfiguration.getChallengeFailedRoutingKey();

        var isCorrect = challengeSolvedDTO.isCorrect();

        if (isCorrect) {
            sendChallengeSolvedMessage(exchange, correctRoutingKey, challengeSolvedDTO);
        } else {
            sendChallengeSolvedMessage(exchange, incorrectRoutingKey, challengeSolvedDTO);
        }
    }

    private void sendChallengeSolvedMessage(String exchange, String routingKey, ChallengeSolvedEvent challengeSolvedEvent) {
        var challengeAttemptId = challengeSolvedEvent.getChallengeAttemptId();
        var correlationData = new CorrelationData(challengeAttemptId);

        var messageProperties = MessagePropertiesBuilder.newInstance()
                .setHeader("x-retry-count", 0)
                .setHeader("x-exchange", exchange)
                .setHeader("x-routing-key", routingKey)
                .build();
        var message = buildMessage(challengeSolvedEvent, messageProperties);
        callbackManager.put(correlationData.getId(), message);
        rabbitTemplate.convertAndSend(exchange, routingKey, message, correlationData);
    }

    private Message buildMessage(ChallengeSolvedEvent challengeSolvedEvent, MessageProperties messageProperties) {
        return rabbitTemplate.getMessageConverter().toMessage(challengeSolvedEvent, messageProperties);
    }
}
