package com.playground.challenge_manager.challenge.services.impl;

import com.playground.challenge_manager.challenge.services.interfaces.ChallengeGeneratorService;
import com.playground.challenge_manager.challenge.services.model.DifficultyLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class ChallengeGeneratorServiceImpl implements ChallengeGeneratorService {

    private static final Random random = new Random();
    private static final int MAX_COUNT = 10;

    /**
     * Generates an array of random numbers within a specified range.
     * The count of numbers generated is capped at 10 to prevent excessive generation.
     *
     * @param difficultyLevel The DifficultyLevel enum
     * @param count The number of random integers to generate.
     * @return An array of random Integers.
     */
    public List<Integer> generate(DifficultyLevel difficultyLevel, int count) {
        validateCount(count);
        var result = new ArrayList<Integer>(count);
        for (int i = 0; i < count; i++) {
            result.add(next(difficultyLevel.getMin(), difficultyLevel.getMax()));
        }
        return result;
    }

    /**
     * Generates a random number within a specified range (inclusive).
     * @param min The minimum value (inclusive).
     * @param max The maximum value (inclusive).
     * @return A random integer within the specified range.
     */
    private int next(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }

    /**
     * Validates that the requested count of numbers is within the allowed range.
     * <p>
     * The count must be a positive integer and must not exceed the predefined
     * {@link #MAX_COUNT}. This method ensures that any request for random numbers
     * is for a valid and reasonable quantity.
     *
     * @param count The number of integers to validate.
     * @throws IllegalArgumentException if the count is less than or equal to 0,
     *                                  or greater than {@link #MAX_COUNT}.
     */
    private void validateCount(int count) {
        if (count <= 0 || count > MAX_COUNT) {
            log.error("Invalid count requested: {}. Count must be between 1 and {}.", count, MAX_COUNT);
            throw new IllegalArgumentException("Count must be between 1 and " + MAX_COUNT);
        }
    }
}
