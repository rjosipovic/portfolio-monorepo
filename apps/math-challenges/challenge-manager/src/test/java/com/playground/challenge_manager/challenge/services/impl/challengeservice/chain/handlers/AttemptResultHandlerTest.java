package com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.handlers;


import com.playground.challenge_manager.challenge.api.dto.ChallengeAttemptDTO;
import com.playground.challenge_manager.challenge.mappers.ChallengeMapper;
import com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.AttemptVerifierContext;
import com.playground.challenge_manager.challenge.services.model.ChallengeAttempt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class AttemptResultHandlerTest {

    private AttemptResultHandler attemptResultHandler;

    @BeforeEach
    void setUp() {
        ChallengeMapper challengeMapper = Mappers.getMapper(ChallengeMapper.class);
        attemptResultHandler = new AttemptResultHandler(challengeMapper);
    }

    @Test
    void shouldCreateCorrectAttemptResult() {
        //given
        var userId = UUID.randomUUID();
        var firstNumber = 12;
        var secondNumber = 23;
        var guess = 276;
        var isCorrect = true;
        var game = "multiplication";
        var difficulty = "easy";
        var attempt = ChallengeAttemptDTO.builder()
                .userId(userId.toString())
                .firstNumber(firstNumber)
                .secondNumber(secondNumber)
                .guess(guess)
                .game(game)
                .build();
        var challengeAttempt = ChallengeAttempt.builder()
                .userId(userId)
                .firstNumber(firstNumber)
                .secondNumber(secondNumber)
                .resultAttempt(guess)
                .correct(isCorrect)
                .game(game)
                .difficulty(difficulty)
                .build();
        var ctx = new AttemptVerifierContext(attempt);
        ctx.setChallengeAttempt(challengeAttempt);

        //when
        attemptResultHandler.handle(ctx);

        //then
        var result = ctx.getChallengeResult();
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(userId.toString(), result.getUserId()),
                () -> assertEquals(firstNumber, result.getFirstNumber()),
                () -> assertEquals(secondNumber, result.getSecondNumber()),
                () -> assertEquals(guess, result.getGuess()),
                () -> assertEquals(firstNumber * secondNumber, result.getCorrectResult()),
                () -> assertTrue(result.isCorrect())
        );
    }

    @Test
    void shouldCreateIncorrectAttemptResult() {
        //given
        var userId = UUID.randomUUID();
        var firstNumber = 12;
        var secondNumber = 23;
        var guess = 456;
        var game = "multiplication";
        var difficulty = "easy";
        var attempt = ChallengeAttemptDTO.builder()
                .userId(userId.toString())
                .firstNumber(firstNumber)
                .secondNumber(secondNumber)
                .guess(guess)
                .game(game)
                .build();
        var challengeAttempt = ChallengeAttempt.builder()
                .userId(userId)
                .firstNumber(firstNumber)
                .secondNumber(secondNumber)
                .resultAttempt(guess)
                .correct(false)
                .game(game)
                .difficulty(difficulty)
                .build();
        var ctx = new AttemptVerifierContext(attempt);
        ctx.setChallengeAttempt(challengeAttempt);

        //when
        attemptResultHandler.handle(ctx);

        //then
        var result = ctx.getChallengeResult();
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(userId.toString(), result.getUserId()),
                () -> assertEquals(firstNumber, result.getFirstNumber()),
                () -> assertEquals(secondNumber, result.getSecondNumber()),
                () -> assertEquals(guess, result.getGuess()),
                () -> assertEquals(firstNumber * secondNumber, result.getCorrectResult()),
                () -> assertFalse(result.isCorrect())
        );
    }
}