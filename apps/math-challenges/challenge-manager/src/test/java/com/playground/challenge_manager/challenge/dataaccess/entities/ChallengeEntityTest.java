package com.playground.challenge_manager.challenge.dataaccess.entities;

import com.playground.challenge_manager.challenge.services.model.ChallengeStatus;
import com.playground.challenge_manager.challenge.services.model.DifficultyLevel;
import com.playground.challenge_manager.challenge.services.model.OperationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChallengeEntityTest {

    @Test
    @DisplayName("create: Should create entity successfully with valid parameters")
    void create_Success() {
        // given
        var operands = List.of(10, 20);
        var operation = OperationType.ADDITION;
        var difficulty = DifficultyLevel.EASY;
        var userId = UUID.randomUUID();

        // when
        var entity = ChallengeEntity.create(operands, operation, difficulty, userId);

        // then
        assertAll(
                () -> assertNotNull(entity),
                () -> assertEquals(operands, entity.getOperands()),
                () -> assertEquals(operation, entity.getOperationType()),
                () -> assertEquals(difficulty, entity.getDifficulty()),
                () -> assertEquals(userId, entity.getUserId()),
                () -> assertEquals(ChallengeStatus.GENERATED, entity.getStatus())
        );
    }

    @Test
    @DisplayName("create: Should throw IllegalArgumentException for null operands")
    void create_NullOperands() {
        // given
        var operation = OperationType.ADDITION;
        var difficulty = DifficultyLevel.EASY;
        var userId = UUID.randomUUID();

        // when & then
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            ChallengeEntity.create(null, operation, difficulty, userId);
        });
        assertEquals("Operands list must not be null and must contain at least 2 numbers.", exception.getMessage());
    }

    @Test
    @DisplayName("create: Should throw IllegalArgumentException for empty operands")
    void create_EmptyOperands() {
        // given
        var operation = OperationType.ADDITION;
        var difficulty = DifficultyLevel.EASY;
        var userId = UUID.randomUUID();

        // when & then
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            ChallengeEntity.create(Collections.emptyList(), operation, difficulty, userId);
        });
        assertEquals("Operands list must not be null and must contain at least 2 numbers.", exception.getMessage());
    }

    @Test
    @DisplayName("create: Should throw IllegalArgumentException for less than two operands")
    void create_LessThanTwoOperands() {
        // given
        var operation = OperationType.ADDITION;
        var difficulty = DifficultyLevel.EASY;
        var userId = UUID.randomUUID();

        // when & then
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            ChallengeEntity.create(List.of(10), operation, difficulty, userId);
        });
        assertEquals("Operands list must not be null and must contain at least 2 numbers.", exception.getMessage());
    }

    @Test
    @DisplayName("create: Should throw IllegalArgumentException for null operation type")
    void create_NullOperationType() {
        // given
        var operands = List.of(10, 20);
        var difficulty = DifficultyLevel.EASY;
        var userId = UUID.randomUUID();

        // when & then
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            ChallengeEntity.create(operands, null, difficulty, userId);
        });
        assertEquals("OperationType must not be null.", exception.getMessage());
    }

    @Test
    @DisplayName("create: Should throw IllegalArgumentException for null difficulty level")
    void create_NullDifficultyLevel() {
        // given
        var operands = List.of(10, 20);
        var operation = OperationType.ADDITION;
        var userId = UUID.randomUUID();

        // when & then
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            ChallengeEntity.create(operands, operation, null, userId);
        });
        assertEquals("DifficultyLevel must not be null.", exception.getMessage());
    }

    @Test
    @DisplayName("create: Should throw IllegalArgumentException for null user ID")
    void create_NullUserId() {
        // given
        var operands = List.of(10, 20);
        var operation = OperationType.ADDITION;
        var difficulty = DifficultyLevel.EASY;

        // when & then
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            ChallengeEntity.create(operands, operation, difficulty, null);
        });
        assertEquals("UserId must not be null.", exception.getMessage());
    }
}
