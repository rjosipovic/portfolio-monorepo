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

@DisplayName("DivisionCalculator Test")
class DivisionCalculatorTest {

    private DivisionCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new DivisionCalculator();
    }

    @DisplayName("Should return correct quotient for valid inputs")
    @ParameterizedTest(name = "Run {index}: {0} / {1} = {2}")
    @MethodSource("validDivisionTestCases")
    void testCalculate_Success(int numerator, int denominator, int expectedQuotient) {
        // Given
        var operands = List.of(numerator, denominator);

        // When
        int actualQuotient = calculator.calculate(operands);

        // Then
        assertEquals(expectedQuotient, actualQuotient);
    }

    private static Stream<Arguments> validDivisionTestCases() {
        return Stream.of(
                Arguments.of(10, 2, 5),
                Arguments.of(10, 3, 3),      // Integer division
                Arguments.of(-10, 2, -5),
                Arguments.of(10, -2, -5),
                Arguments.of(-10, -2, 5),
                Arguments.of(0, 5, 0)
        );
    }

    @Test
    @DisplayName("Should throw ArithmeticException for division by zero")
    void testCalculate_ThrowsException_ForDivisionByZero() {
        // Given
        List<Integer> operands = List.of(10, 0);

        // When & Then
        ArithmeticException exception = assertThrows(ArithmeticException.class, () -> {
            calculator.calculate(operands);
        }, "calculate() should throw ArithmeticException when denominator is zero");

        assertEquals("Cannot divide by zero.", exception.getMessage());
    }

    @DisplayName("Should throw IllegalArgumentException for invalid number of operands")
    @ParameterizedTest(name = "Run {index}: operands={0}")
    @MethodSource("invalidOperandCountCases")
    void testCalculate_ThrowsException_ForInvalidOperandCount(List<Integer> operands) {
        // When & Then
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            calculator.calculate(operands);
        }, "calculate() should throw IllegalArgumentException for non-2 operand lists");

        assertEquals("Division operation requires exactly two operands.", exception.getMessage());
    }

    private static Stream<Arguments> invalidOperandCountCases() {
        return Stream.of(
                Arguments.of(List.of(10)),
                Arguments.of(List.of(1, 2, 3)),
                Arguments.of(Collections.emptyList())
        );
    }

    @Test
    @DisplayName("Should return OperationType.DIVISION")
    void shouldReturnCorrectOperationType() {
        // When
        var type = calculator.getOperationType();

        // Then
        assertEquals(OperationType.DIVISION, type);
    }
}