package com.playground.challenge_manager.challenge.services.impl;

import com.playground.challenge_manager.challenge.services.model.DifficultyLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChallengeGeneratorServiceImplTest {

    private final ChallengeGeneratorServiceImpl challengeGeneratorService = new ChallengeGeneratorServiceImpl();

    @DisplayName("Should generate two random numbers within the correct range for each difficulty level")
    @ParameterizedTest(name = "For level {0}, numbers should be between {1} and {2}")
    @CsvSource({
            "EASY, 1, 9",
            "MEDIUM, 10, 99",
            "HARD, 100, 999",
            "EXPERT, 1000, 9999"
    })
    void shouldGenerateTwoNumbersForEachDifficulty(DifficultyLevel difficulty, int min, int max) {
        // given
        var count = 2;

        // when
        var result = challengeGeneratorService.generate(difficulty, count);

        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(count, result.size()),
                () -> assertTrue(result.get(0) >= min && result.get(0) <= max,"First number is out of range for level: " + difficulty),
                () -> assertTrue(result.get(1) >= min && result.get(1) <= max,"Second number is out of range for level: " + difficulty)
        );
    }

    @Test
    @DisplayName("Should generate more than two numbers when requested")
    void shouldGenerateMoreThanTwoNumbers() {
        // given
        var difficultyLevel = DifficultyLevel.EXPERT;
        var count = 5;

        // when
        var result = challengeGeneratorService.generate(difficultyLevel, count);

        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(count, result.size()),
                () -> assertTrue(result.get(0) >= difficultyLevel.getMin() && result.get(0) <= difficultyLevel.getMax()),
                () -> assertTrue(result.get(1) >= difficultyLevel.getMin() && result.get(1) <= difficultyLevel.getMax()),
                () -> assertTrue(result.get(2) >= difficultyLevel.getMin() && result.get(2) <= difficultyLevel.getMax()),
                () -> assertTrue(result.get(3) >= difficultyLevel.getMin() && result.get(3) <= difficultyLevel.getMax()),
                () -> assertTrue(result.get(4) >= difficultyLevel.getMin() && result.get(4) <= difficultyLevel.getMax())
        );
    }

    @Test
    @DisplayName("Should throw an exception for invalid count")
    void shouldThrowExceptionForInvalidCount() {
        // given
        var difficultyLevel = DifficultyLevel.EASY;
        var invalidCount = -1;

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            challengeGeneratorService.generate(difficultyLevel, invalidCount);
        });
    }
}
