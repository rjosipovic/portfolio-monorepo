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

@DisplayName("MultiplicationCalculator Test")
class MultiplicationCalculatorTest {

    private MultiplicationCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new MultiplicationCalculator();
    }

    @DisplayName("Should return correct product for various inputs")
    @ParameterizedTest(name = "Run {index}: operands={0}, expectedProduct={1}")
    @MethodSource("multiplicationTestCases")
    void testCalculate(List<Integer> operands, int expectedProduct) {
        // When
        var actualProduct = calculator.calculate(operands);

        // Then
        assertEquals(expectedProduct, actualProduct,
                () -> "The product of " + operands + " should be " + expectedProduct);
    }

    private static Stream<Arguments> multiplicationTestCases() {
        return Stream.of(
                // Standard cases
                Arguments.of(List.of(5, 10), 50),
                Arguments.of(List.of(2, 3, 4), 24),

                // Negative numbers
                Arguments.of(List.of(-5, 10), -50),
                Arguments.of(List.of(-5, -10), 50),
                Arguments.of(List.of(-2, -3, -4), -24),

                // Zero handling
                Arguments.of(List.of(100, 0), 0),
                Arguments.of(List.of(0, 5, 10), 0),

                // Edge cases
                Arguments.of(List.of(7), 7), // Single operand
                Arguments.of(Collections.emptyList(), 1) // Identity value for multiplication
        );
    }

    @Test
    @DisplayName("Should return OperationType.MULTIPLICATION")
    void shouldReturnCorrectOperationType() {
        // When
        var type = calculator.getOperationType();

        // Then
        assertEquals(OperationType.MULTIPLICATION, type);
    }
}