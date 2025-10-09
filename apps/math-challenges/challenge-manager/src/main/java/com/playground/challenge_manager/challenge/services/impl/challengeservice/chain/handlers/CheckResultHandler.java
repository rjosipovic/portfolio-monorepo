package com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.handlers;


import com.playground.challenge_manager.challenge.api.dto.ChallengeAttemptDTO;
import com.playground.challenge_manager.challenge.mappers.ChallengeMapper;
import com.playground.challenge_manager.challenge.services.config.ChallengeConfig;
import com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.AttemptHandler;
import com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.AttemptVerifierContext;
import com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.util.MathUtil;
import com.playground.challenge_manager.challenge.services.model.ChallengeAttempt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckResultHandler implements AttemptHandler {

    private final ChallengeConfig challengeConfig;
    private final ChallengeMapper challengeMapper;

    @Override
    public void handle(AttemptVerifierContext ctx) {
        var attempt = ctx.getAttempt();
        var challengeAttempt = build(attempt);
        ctx.setChallengeAttempt(challengeAttempt);
    }

    private ChallengeAttempt build(ChallengeAttemptDTO dto) {
        var mapped = challengeMapper.toChallengeAttempt(dto);
        var difficulty = determineDifficulty(mapped.getFirstNumber(), mapped.getSecondNumber());
        var correct = isCorrect(mapped.getResultAttempt(), MathUtil.calculateResult(mapped.getFirstNumber(), mapped.getSecondNumber(), mapped.getGame()));
        return mapped.toBuilder().correct(correct).difficulty(difficulty).build();
    }

    private String determineDifficulty(int firstNumber, int secondNumber) {
        return challengeConfig.getDifficultyLevels().stream()
                .filter(difficulty -> isInRange(firstNumber, difficulty.getMin(), difficulty.getMax()) && isInRange(secondNumber, difficulty.getMin(), difficulty.getMax()))
                .findFirst()
                .map(ChallengeConfig.DifficultyLevel::getLevel)
                .orElseThrow(() -> new IllegalArgumentException("No difficulty level found for numbers: " + firstNumber + " and " + secondNumber));
    }

    private static boolean isInRange(int number, int min, int max) {
        return number >= min && number <= max;
    }

    private boolean isCorrect(int guess, int correctResult) {
        return guess == correctResult;
    }
}
