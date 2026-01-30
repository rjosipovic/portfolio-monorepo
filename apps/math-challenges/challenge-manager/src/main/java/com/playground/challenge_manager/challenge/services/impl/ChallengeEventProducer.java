package com.playground.challenge_manager.challenge.services.impl;

import com.playground.challenge_manager.challenge.services.model.ChallengeCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChallengeEventProducer {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishChallengeCreated(UUID challengeId) {
        var event = new ChallengeCreatedEvent(challengeId);
        applicationEventPublisher.publishEvent(event);
    }
}
