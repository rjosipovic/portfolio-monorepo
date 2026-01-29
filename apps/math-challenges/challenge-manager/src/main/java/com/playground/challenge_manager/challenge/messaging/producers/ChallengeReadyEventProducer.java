package com.playground.challenge_manager.challenge.messaging.producers;

import com.playground.challenge_manager.challenge.messaging.MessagingConfiguration;
import com.playground.challenge_manager.challenge.messaging.events.ChallengeReadyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChallengeReadyEventProducer {

    private final RabbitTemplate rabbitTemplate;
    private final MessagingConfiguration messagingConfiguration;

    public void publishChallengeReadyEvent(ChallengeReadyEvent event) {
        var exchangeName = messagingConfiguration.getChallengeUpdate().getFanoutExchange();
        rabbitTemplate.convertAndSend(exchangeName, "", event);
        log.info("Published ChallengeReadyEvent for challenge {} to exchange {}", event.getChallenge().getId(), exchangeName);
    }
}
