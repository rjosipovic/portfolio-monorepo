package com.playground.challenge_manager.challenge.messaging.consumers;

import com.playground.challenge_manager.challenge.messaging.events.ChallengeReadyEvent;
import com.playground.challenge_manager.challenge.services.impl.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChallengeUpdateListener {

    private final SseService sseService;

    @RabbitListener(queues = "#{challengeUpdateQueue.name}") // SpEL to get the generated queue name
    public void handleChallengeUpdate(ChallengeReadyEvent event) {
        log.info("Received ChallengeReadyEvent for challenge {}", event.getChallenge().getId());
        sseService.sendToClient(event.getChallenge().getId(), event.getChallenge());
    }
}
