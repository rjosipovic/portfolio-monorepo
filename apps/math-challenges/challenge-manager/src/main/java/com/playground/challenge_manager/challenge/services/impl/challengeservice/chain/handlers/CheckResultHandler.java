package com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.handlers;


import com.playground.challenge_manager.challenge.services.config.ChallengeConfig;
import com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.AttemptHandler;
import com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.AttemptVerifierContext;
import com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.util.MathUtil;
import com.playground.challenge_manager.challenge.services.model.ChallengeAttempt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckResultHandler implements AttemptHandler {

    private final ChallengeConfig challengeConfig;

    @Override
    public void handle(AttemptVerifierContext ctx) {
        var attempt = ctx.getAttempt();
        var userId = UUID.fromString(attempt.getUserId());
        var firstNumber = attempt.getFirstNumber();
        var secondNumber = attempt.getSecondNumber();
        var guess = attempt.getGuess();
        var game = attempt.getGame();
        var difficulty = determineDifficulty(firstNumber, secondNumber);
        var isCorrect = isCorrect(guess, MathUtil.calculateResult(firstNumber, secondNumber, game));

        var challengeAttempt = ChallengeAttempt.builder()
                .userId(userId)
                .firstNumber(firstNumber)
                .secondNumber(secondNumber)
                .resultAttempt(guess)
                .correct(isCorrect)
                .game(game)
                .difficulty(difficulty)
                .build();
        ctx.setChallengeAttempt(challengeAttempt);
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
