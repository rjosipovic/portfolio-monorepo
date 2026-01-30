package com.playground.challenge_manager.challenge.services.impl.calculation;

import com.playground.challenge_manager.challenge.services.model.OperationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CalculatorFactory Test")
class CalculatorFactoryTest {

    @Mock
    private AdditionCalculator additionCalculator;
    @Mock
    private SubtractionCalculator subtractionCalculator;
    @Mock
    private MultiplicationCalculator multiplicationCalculator;
    @Mock
    private DivisionCalculator divisionCalculator;

    private CalculatorFactory calculatorFactory;

    @BeforeEach
    void setUp() {
        // --- Arrange ---
        // Configure each mock to report its OperationType
        when(additionCalculator.getOperationType()).thenReturn(OperationType.ADDITION);
        when(subtractionCalculator.getOperationType()).thenReturn(OperationType.SUBTRACTION);
        when(multiplicationCalculator.getOperationType()).thenReturn(OperationType.MULTIPLICATION);
        when(divisionCalculator.getOperationType()).thenReturn(OperationType.DIVISION);

        // Manually create the factory, injecting a list of our mocks
        // This simulates the Spring dependency injection process
        var calculators = List.of(
                additionCalculator,
                subtractionCalculator,
                multiplicationCalculator,
                divisionCalculator
        );
        calculatorFactory = new CalculatorFactory(calculators);

        // Manually trigger the @PostConstruct method
        // In a real Spring context, this is called automatically after the constructor.
        // In a unit test, we must call it ourselves.
        try {
            var initMethod = CalculatorFactory.class.getDeclaredMethod("init");
            initMethod.setAccessible(true);
            initMethod.invoke(calculatorFactory);
        } catch (Exception e) {
            fail("Failed to invoke init method via reflection", e);
        }
    }

    @Test
    @DisplayName("Should return the correct calculator for each operation type")
    void shouldReturnCorrectCalculator() {
        // --- Act & Assert ---
        // Verify that the factory returns the exact mock instance we expect for each type
        assertSame(additionCalculator, calculatorFactory.getCalculator(OperationType.ADDITION), "Should return AdditionCalculator for ADDITION");
        assertSame(subtractionCalculator, calculatorFactory.getCalculator(OperationType.SUBTRACTION), "Should return SubtractionCalculator for SUBTRACTION");
        assertSame(multiplicationCalculator, calculatorFactory.getCalculator(OperationType.MULTIPLICATION), "Should return MultiplicationCalculator for MULTIPLICATION");
        assertSame(divisionCalculator, calculatorFactory.getCalculator(OperationType.DIVISION), "Should return DivisionCalculator for DIVISION");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for an unknown operation type")
    void shouldThrowExceptionForUnknownType() {
        // --- Act & Assert ---
        // Verify that asking for a non-existent calculator throws the correct exception
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            // We don't have a mock for this, so it should fail
            calculatorFactory.getCalculator(null);
        });

        assertTrue(exception.getMessage().contains("No calculator found for type: null"));
    }
}