package com.playground.gamification_manager.game.dataaccess.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ScoreEntityTest {

    @Test
    void whenCreateScoreWithNullUserId_thenThrowException() {
        // given
        var challengeAttemptId = UUID.randomUUID();
        var score = 10;

        // when & then
        assertThrows(IllegalArgumentException.class, () -> ScoreEntity.create(null, challengeAttemptId, score));
    }

    @Test
    void whenCreateScoreWithNullChallengeAttemptId_thenThrowException() {
        // given
        var userId = UUID.randomUUID();
        var score = 10;

        // when & then
        assertThrows(IllegalArgumentException.class, () -> ScoreEntity.create(userId, null, score));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -10})
    void whenCreateScoreWithNonPositiveScore_thenThrowException(int invalidScore) {
        // given
        var userId = UUID.randomUUID();
        var challengeAttemptId = UUID.randomUUID();

        // when & then
        assertThrows(IllegalArgumentException.class, () -> ScoreEntity.create(userId, challengeAttemptId, invalidScore));
    }
}
