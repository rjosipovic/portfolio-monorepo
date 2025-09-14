package com.playground.analytics_manager.inbound.challenge;

import com.playground.analytics_manager.dataaccess.entity.ChallengeEntity;
import com.playground.analytics_manager.dataaccess.entity.UserAttempt;
import com.playground.analytics_manager.dataaccess.repository.ChallengeRepository;
import com.playground.analytics_manager.dataaccess.repository.UserRepository;
import com.playground.analytics_manager.inbound.messaging.events.ChallengeSolvedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChallengeServiceImpl implements ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public void process(ChallengeSolvedEvent event) {
        if (!isEventValid(event)) {
            log.warn("Invalid ChallengeSolvedEvent received. Ignoring. Event: {}", event);
            return;
        }

        var challengeId = UUID.fromString(event.getChallengeAttemptId());
        var userId = UUID.fromString(event.getUserId());

        // Defensively check if we've already processed this attempt
        if (challengeRepository.existsByIdAndUserAttempt_User_Id(challengeId, userId)) {
            log.warn("Duplicate ChallengeSolvedEvent received for user {}. Ignoring. Attempt ID: {}", userId, challengeId);
            return;
        }

        var firstNumber = event.getFirstNumber();
        var secondNumber = event.getSecondNumber();
        var resultAttempt = event.getResultAttempt();
        var correct = event.getCorrect();
        var game = event.getGame();
        var difficulty = event.getDifficulty();
        var attemptDate = event.getAttemptDate();

        var userEntityOptional = userRepository.findById(userId);

        if (userEntityOptional.isEmpty()) {
            return;
        }

        var userEntity = userEntityOptional.get();
        var userAttempt = UserAttempt.create(
                attemptDate,
                resultAttempt,
                correct,
                userEntity
        );
        var challengeEntity = ChallengeEntity.create(
                challengeId,
                firstNumber,
                secondNumber,
                game,
                difficulty,
                userAttempt
        );

        challengeRepository.save(challengeEntity);
        applicationEventPublisher.publishEvent(event);
    }

    private boolean isEventValid(ChallengeSolvedEvent event) {
        return Objects.nonNull(event.getUserId()) &&
                Objects.nonNull(event.getChallengeAttemptId()) &&
                Objects.nonNull(event.getFirstNumber()) &&
                Objects.nonNull(event.getSecondNumber()) &&
                Objects.nonNull(event.getResultAttempt()) &&
                Objects.nonNull(event.getCorrect()) &&
                Objects.nonNull(event.getGame()) &&
                Objects.nonNull(event.getDifficulty()) &&
                Objects.nonNull(event.getAttemptDate());
    }
}
