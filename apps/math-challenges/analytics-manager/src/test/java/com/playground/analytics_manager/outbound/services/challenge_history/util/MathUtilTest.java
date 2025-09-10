package com.playground.analytics_manager.outbound.services.challenge_history.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MathUtilTest {

    @DisplayName("Should calculate correct result for all valid operations")
    @ParameterizedTest(name = "{0} {1} {2} = {3}")
    @CsvSource({
            "10, 5, addition, 15",
            "10, 5, subtraction, 5",
            "10, 5, multiplication, 50",
            "10, 5, division, 2",
            "7, 3, addition, 10",
            "7, 3, subtraction, 4",
            "7, 3, multiplication, 21",
            "7, 3, division, 2" // Integer division
    })
    void shouldCalculateCorrectResultForValidOperations(int first, int second, String operation, int expected) {
        int result = MathUtil.calculateResult(first, second, operation);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid operation string")
    void shouldThrowExceptionForInvalidOperation() {
        assertThrows(IllegalArgumentException.class,
                () -> MathUtil.calculateResult(10, 5, "invalid-operation"));
    }

    @Test
    @DisplayName("Should throw ArithmeticException for division by zero")
    void shouldThrowExceptionForDivisionByZero() {
        assertThrows(ArithmeticException.class,
                () -> MathUtil.calculateResult(10, 0, "division"));
    }
}