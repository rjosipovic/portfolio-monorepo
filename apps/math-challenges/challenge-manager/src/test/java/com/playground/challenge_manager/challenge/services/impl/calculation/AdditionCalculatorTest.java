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

@DisplayName("AdditionCalculator Test")
class AdditionCalculatorTest {

    private AdditionCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new AdditionCalculator();
    }

    @DisplayName("Should return correct sum for various inputs")
    @ParameterizedTest(name = "Run {index}: operands={0}, expectedSum={1}")
    @MethodSource("additionTestCases")
    void testCalculate(List<Integer> operands, int expectedSum) {
        // When
        var actualSum = calculator.calculate(operands);

        // Then
        assertEquals(expectedSum, actualSum,
                () -> "The sum of " + operands + " should be " + expectedSum);
    }

    private static Stream<Arguments> additionTestCases() {
        return Stream.of(
                Arguments.of(List.of(5, 10), 15),
                Arguments.of(List.of(1, 2, 3, 4, 5), 15),
                Arguments.of(List.of(-5, 10), 5),
                Arguments.of(List.of(-5, -10), -15),
                Arguments.of(List.of(100, 0), 100),
                Arguments.of(List.of(7), 7),
                Arguments.of(Collections.emptyList(), 0)
        );
    }

    @Test
    @DisplayName("Should return OperationType.ADDITION")
    void shouldReturnCorrectOperationType() {
        // When
        var type = calculator.getOperationType();

        // Then
        assertEquals(OperationType.ADDITION, type,"The calculator should identify itself with OperationType.ADDITION");
    }
}
