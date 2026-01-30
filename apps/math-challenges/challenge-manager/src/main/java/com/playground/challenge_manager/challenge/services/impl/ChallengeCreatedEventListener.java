package com.playground.challenge_manager.challenge.services.impl;

import com.playground.challenge_manager.challenge.services.interfaces.ChallengeProcessor;
import com.playground.challenge_manager.challenge.services.model.ChallengeCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChallengeCreatedEventListener {

    private final ChallengeProcessor challengeProcessor;

    @EventListener
    public void onChallengeCreatedEvent(ChallengeCreatedEvent event) {
        log.info("Challenge created event received:{}", event);
        var challengeId = event.getChallengeId();
        challengeProcessor.process(challengeId);
    }
}
