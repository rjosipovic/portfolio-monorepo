package com.playground.challenge_manager.challenge.dataaccess.entities;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChallengeAttemptEntityTest {

    @Test
    void whenCreate_withValidData_thenAllFieldsAreSet() {
        // given
        var userId = UUID.randomUUID();
        var firstNumber = 50;
        var secondNumber = 70;
        var resultAttempt = 3500;
        var correct = true;
        var game = "multiplication";
        var difficulty = "medium";

        // when
        var attempt = ChallengeAttemptEntity.create(
                userId, firstNumber, secondNumber, resultAttempt, correct, game, difficulty
        );

        // then
        assertAll(
                () -> assertNotNull(attempt),
                () -> assertEquals(userId, attempt.getUserId()),
                () -> assertEquals(firstNumber, attempt.getFirstNumber()),
                () -> assertEquals(secondNumber, attempt.getSecondNumber()),
                () -> assertEquals(resultAttempt, attempt.getResultAttempt()),
                () -> assertEquals(correct, attempt.getCorrect()),
                () -> assertEquals(game, attempt.getGame()),
                () -> assertEquals(difficulty, attempt.getDifficulty())
        );
    }

    @Test
    void whenCreate_withNullUserId_thenThrowException() {
        // when & then
        assertThrows(IllegalArgumentException.class, () -> ChallengeAttemptEntity.create(
                null, 50, 70, 3500, true, "multiplication", "medium"
        ));
    }

    @Test
    void whenCreate_withNullGame_thenThrowException() {
        // when & then
        assertThrows(IllegalArgumentException.class, () -> ChallengeAttemptEntity.create(
                UUID.randomUUID(), 50, 70, 3500, true, null, "medium"
        ));
    }

    @Test
    void whenCreate_withNullDifficulty_thenThrowException() {
        // when & then
        assertThrows(IllegalArgumentException.class, () -> ChallengeAttemptEntity.create(
                UUID.randomUUID(), 50, 70, 3500, true, "multiplication", null
        ));
    }
}
