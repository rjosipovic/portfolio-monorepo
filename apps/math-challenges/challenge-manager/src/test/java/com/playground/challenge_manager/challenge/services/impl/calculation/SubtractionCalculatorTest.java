package com.playground.challenge_manager.challenge.services.impl.calculation;

import com.playground.challenge_manager.challenge.services.model.OperationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("SubtractionCalculator Test")
class SubtractionCalculatorTest {

    private SubtractionCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new SubtractionCalculator();
    }

    @DisplayName("Should return correct difference for valid inputs")
    @ParameterizedTest(name = "Run {index}: {0} - {1} = {2}")
    @MethodSource("validSubtractionTestCases")
    void testCalculate_Success(int first, int second, int expectedDifference) {
        // Given
        List<Integer> operands = List.of(first, second);

        // When
        int actualDifference = calculator.calculate(operands);

        // Then
        assertEquals(expectedDifference, actualDifference);
    }

    private static Stream<Arguments> validSubtractionTestCases() {
        return Stream.of(
                Arguments.of(10, 3, 7),
                Arguments.of(3, 10, -7),
                Arguments.of(-5, 5, -10),
                Arguments.of(5, -5, 10),
                Arguments.of(-5, -5, 0),
                Arguments.of(100, 0, 100),
                Arguments.of(0, 100, -100)
        );
    }

    @DisplayName("Should throw IllegalArgumentException for invalid number of operands")
    @ParameterizedTest(name = "Run {index}: operands={0}")
    @MethodSource("invalidOperandCountCases")
    void testCalculate_ThrowsException_ForInvalidOperandCount(List<Integer> operands) {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            calculator.calculate(operands);
        }, "calculate() should throw IllegalArgumentException for non-2 operand lists");

        assertEquals("Subtraction operation requires exactly two operands.", exception.getMessage());
    }

    private static Stream<Arguments> invalidOperandCountCases() {
        return Stream.of(
                Arguments.of(List.of(10)),
                Arguments.of(List.of(1, 2, 3)),
                Arguments.of(Collections.emptyList())
        );
    }

    @Test
    @DisplayName("Should return OperationType.SUBTRACTION")
    void shouldReturnCorrectOperationType() {
        // When
        OperationType type = calculator.getOperationType();

        // Then
        assertEquals(OperationType.SUBTRACTION, type);
    }
}