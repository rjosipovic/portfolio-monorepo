package com.playground.challenge_manager.challenge.services.impl.calculation;

import com.playground.challenge_manager.challenge.services.interfaces.OperationCalculator;
import com.playground.challenge_manager.challenge.services.model.OperationType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DivisionCalculator implements OperationCalculator {

    @Override
    public int calculate(List<Integer> operands) {
        // Enforce exactly 2 operands for clarity
        if (operands.size() != 2) {
            throw new IllegalArgumentException("Division operation requires exactly two operands.");
        }

        int numerator = operands.get(0);
        int denominator = operands.get(1);

        // Prevent division by zero
        if (denominator == 0) {
            throw new ArithmeticException("Cannot divide by zero.");
        }

        // Perform integer division
        return numerator / denominator;
    }

    @Override
    public OperationType getOperationType() {
        return OperationType.DIVISION;
    }
}
