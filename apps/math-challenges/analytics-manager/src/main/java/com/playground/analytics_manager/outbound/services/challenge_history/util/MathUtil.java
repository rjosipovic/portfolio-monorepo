package com.playground.analytics_manager.outbound.services.challenge_history.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MathUtil {

    public static int calculateResult(int firstNumber, int secondNumber, String game) {
        // First, convert the string to our safe enum type.
        // This will throw an exception if the 'game' string is invalid.
        var operation = GameOperation.fromString(game);

        return switch (operation) {
            case ADDITION -> firstNumber + secondNumber;
            case SUBTRACTION -> firstNumber - secondNumber;
            case MULTIPLICATION -> firstNumber * secondNumber;
            case DIVISION -> firstNumber / secondNumber;
        };
    }
}
