package com.playground.gamification_manager.game.messaging.consumers;

import com.playground.gamification_manager.game.messaging.events.ChallengeSolvedEvent;
import com.playground.gamification_manager.game.service.interfaces.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChallengesSolvedConsumer {

    private final GameService gameService;

    @RabbitListener(queues = "#{challengeSolvedCorrectQueue.name}", ackMode = "AUTO")
    public void handleChallengeSolved(ChallengeSolvedEvent event) {
        log.info("Received challenge solved event: {}", event);
        gameService.process(event);
    }
}
