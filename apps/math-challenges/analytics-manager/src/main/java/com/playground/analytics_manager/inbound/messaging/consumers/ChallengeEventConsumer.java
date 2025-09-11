package com.playground.analytics_manager.inbound.messaging.consumers;

import com.playground.analytics_manager.inbound.challenge.ChallengeService;
import com.playground.analytics_manager.inbound.messaging.events.ChallengeSolvedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChallengeEventConsumer {

    private final ChallengeService challengeService;

    @RabbitListener(queues = "#{challengeQueue.name}", ackMode = "AUTO")
    public void handleChallengeEvent(ChallengeSolvedEvent challengeSolvedEvent) {
        log.info("Received challenge event: {}", challengeSolvedEvent);
        challengeService.process(challengeSolvedEvent);
    }
}
