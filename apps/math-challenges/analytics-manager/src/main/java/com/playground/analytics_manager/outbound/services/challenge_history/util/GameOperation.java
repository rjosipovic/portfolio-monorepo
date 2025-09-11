package com.playground.analytics_manager.outbound.services.challenge_history.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
public enum GameOperation {
    ADDITION("addition"),
    SUBTRACTION("subtraction"),
    MULTIPLICATION("multiplication"),
    DIVISION("division");

    private final String name;

    // A static map for efficient, case-insensitive lookups.
    private static final Map<String, GameOperation> BY_NAME =
            Stream.of(values()).collect(Collectors.toMap(GameOperation::getName, Function.identity()));

    /**
     * Converts a string value to its corresponding GameOperation enum constant.
     * Throws IllegalArgumentException if the string does not match any operation.
     */
    public static GameOperation fromString(String operationName) {
        var operation = BY_NAME.get(operationName);
        if (Objects.isNull(operation)) {
            throw new IllegalArgumentException("No enum constant for value: " + operationName);
        }
        return operation;
    }
}