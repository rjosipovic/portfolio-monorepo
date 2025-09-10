package com.playground.analytics_manager.inbound.challenge;

import com.playground.analytics_manager.dataaccess.entity.ChallengeEntity;
import com.playground.analytics_manager.dataaccess.entity.UserAttempt;
import com.playground.analytics_manager.dataaccess.repository.ChallengeRepository;
import com.playground.analytics_manager.dataaccess.repository.UserRepository;
import com.playground.analytics_manager.inbound.messaging.events.ChallengeSolvedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChallengeServiceImpl implements ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public void process(ChallengeSolvedEvent event) {
        var userId = event.getUserId();
        var challengeAttemptId = event.getChallengeAttemptId();
        var firstNumber = event.getFirstNumber();
        var secondNumber = event.getSecondNumber();
        var resultAttempt = event.getResultAttempt();
        var correct = event.isCorrect();
        var game = event.getGame();
        var difficulty = event.getDifficulty();
        var attemptDate = event.getAttemptDate();

        var userEntityOptional = userRepository.findById(UUID.fromString(userId));

        if (userEntityOptional.isEmpty()) {
            return;
        }

        var userEntity = userEntityOptional.get();
        var userAttempt = UserAttempt.builder()
                .attemptDate(attemptDate)
                .resultAttempt(resultAttempt)
                .correct(correct)
                .user(userEntity)
                .build();
        var challengeEntity = ChallengeEntity.builder()
                .id(UUID.fromString(challengeAttemptId))
                .firstNumber(firstNumber)
                .secondNumber(secondNumber)
                .game(game)
                .difficulty(difficulty)
                .userAttempt(userAttempt)
                .build();

        challengeRepository.save(challengeEntity);
        applicationEventPublisher.publishEvent(event);
    }
}
