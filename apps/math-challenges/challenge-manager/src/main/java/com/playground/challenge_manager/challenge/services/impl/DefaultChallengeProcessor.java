package com.playground.challenge_manager.challenge.services.impl;

import com.playground.challenge_manager.challenge.dataaccess.repositories.ChallengeRepository;
import com.playground.challenge_manager.challenge.mappers.ChallengeMapper;
import com.playground.challenge_manager.challenge.messaging.events.ChallengeReadyEvent;
import com.playground.challenge_manager.challenge.messaging.producers.ChallengeReadyEventProducer;
import com.playground.challenge_manager.challenge.services.impl.calculation.CalculatorFactory;
import com.playground.challenge_manager.challenge.services.interfaces.ChallengeProcessor;
import com.playground.challenge_manager.challenge.services.model.ChallengeStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultChallengeProcessor implements ChallengeProcessor {

    private static final int EXPIRE_TIME = 5;

    private final ChallengeRepository challengeRepository;
    private final CalculatorFactory calculatorFactory;
    private final ChallengeReadyEventProducer challengeReadyEventProducer; // New producer
    private final ChallengeMapper challengeMapper;

    @Override
    @Transactional
    public void process(UUID challengeId) {
        // 1. Find the challenge
        var challengeEntity = challengeRepository.findById(challengeId).orElseThrow(() -> new EntityNotFoundException("Challenge not found" + challengeId));

        // 2. guard clause: ensure it is the correct state
        if (challengeEntity.getStatus() != ChallengeStatus.GENERATED) {
            log.warn("Challenge [{}] is in an invalid state:{} for processing", challengeId, challengeEntity.getStatus());
            return;
        }

        // 3. Calculate the answer
        var calculator = calculatorFactory.getCalculator(challengeEntity.getOperationType());
        var correctAnswer = calculator.calculate(challengeEntity.getOperands());

        // 4. set the expiry time
        var expiresAt = ZonedDateTime.now().plusMinutes(EXPIRE_TIME);

        // 5. update the entity
        challengeEntity.updateCorrectAnswer(correctAnswer);
        challengeEntity.updateExpiresAt(expiresAt);
        challengeEntity.updateStatus(ChallengeStatus.PENDING);

        // 6. save the entity
        challengeRepository.save(challengeEntity);

        // 7. Publish ChallengeReadyEvent for SSE
        var challengeResponse = challengeMapper.toChallengeResponse(challengeEntity);
        var event = ChallengeReadyEvent.builder().challenge(challengeResponse).build();
        challengeReadyEventProducer.publishChallengeReadyEvent(event);
        log.info("Published ChallengeReadyEvent for challenge {}", challengeId);
    }
}
