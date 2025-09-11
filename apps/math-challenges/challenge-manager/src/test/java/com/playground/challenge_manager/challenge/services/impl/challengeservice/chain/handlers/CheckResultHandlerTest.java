package com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.handlers;

import com.playground.challenge_manager.challenge.api.dto.ChallengeAttemptDTO;
import com.playground.challenge_manager.challenge.services.config.ChallengeConfig;
import com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.AttemptVerifierContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckResultHandlerTest {

    @Mock
    private ChallengeConfig challengeConfig;
    @InjectMocks
    private CheckResultHandler checkResultHandler;

    @Test
    void shouldCheckCorrectResult() {
        //given
        var userId = UUID.randomUUID();
        var firstNumber = 12;
        var secondNumber = 23;
        var guess = 276;
        var game = "multiplication";
        var attempt = ChallengeAttemptDTO.builder()
                .userId(userId.toString())
                .firstNumber(firstNumber)
                .secondNumber(secondNumber)
                .guess(guess)
                .game(game)
                .build();
        var ctx = new AttemptVerifierContext(attempt);
        when(challengeConfig.getDifficultyLevels()).thenReturn(
                List.of(
                        new ChallengeConfig.DifficultyLevel("easy", 1, 9),
                        new ChallengeConfig.DifficultyLevel("medium", 10, 99),
                        new ChallengeConfig.DifficultyLevel("hard", 100, 999),
                        new ChallengeConfig.DifficultyLevel("expert", 1000, 9999)
                )
        );

        //when
        checkResultHandler.handle(ctx);

        //then
        var challengeAttempt = ctx.getChallengeAttempt();
        assertAll(
                () -> assertNotNull(challengeAttempt),
                () -> assertEquals(userId, challengeAttempt.getUserId()),
                () -> assertEquals(firstNumber, challengeAttempt.getFirstNumber()),
                () -> assertEquals(secondNumber, challengeAttempt.getSecondNumber()),
                () -> assertEquals(guess, challengeAttempt.getResultAttempt()),
                () -> assertTrue(challengeAttempt.getCorrect())
        );
    }

    @Test
    void shouldCheckIncorrectResult() {
        //given
        var userId = UUID.randomUUID();
        var firstNumber = 12;
        var secondNumber = 23;
        var guess = 2764;
        var game = "multiplication";
        var attempt = ChallengeAttemptDTO.builder()
                .userId(userId.toString())
                .firstNumber(firstNumber)
                .secondNumber(secondNumber)
                .guess(guess)
                .game(game)
                .build();
        var ctx = new AttemptVerifierContext(attempt);
        when(challengeConfig.getDifficultyLevels()).thenReturn(
                List.of(
                        new ChallengeConfig.DifficultyLevel("easy", 1, 9),
                        new ChallengeConfig.DifficultyLevel("medium", 10, 99),
                        new ChallengeConfig.DifficultyLevel("hard", 100, 999),
                        new ChallengeConfig.DifficultyLevel("expert", 1000, 9999)
                )
        );

        //when
        checkResultHandler.handle(ctx);

        //then
        var challengeAttempt = ctx.getChallengeAttempt();
        assertAll(
                () -> assertNotNull(challengeAttempt),
                () -> assertEquals(userId, challengeAttempt.getUserId()),
                () -> assertEquals(firstNumber, challengeAttempt.getFirstNumber()),
                () -> assertEquals(secondNumber, challengeAttempt.getSecondNumber()),
                () -> assertEquals(guess, challengeAttempt.getResultAttempt()),
                () -> assertFalse(challengeAttempt.getCorrect())
        );
    }
}