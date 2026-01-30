package com.playground.challenge_manager.challenge.services.impl.calculation;

import com.playground.challenge_manager.challenge.services.interfaces.OperationCalculator;
import com.playground.challenge_manager.challenge.services.model.OperationType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SubtractionCalculator implements OperationCalculator {

    @Override
    public int calculate(List<Integer> operands) {
        // Enforce exactly 2 operands for clarity
        if (operands.size() != 2) {
            throw new IllegalArgumentException("Subtraction operation requires exactly two operands.");
        }

        int first = operands.get(0);
        int second = operands.get(1);

        return first - second;
    }

    @Override
    public OperationType getOperationType() {
        return OperationType.SUBTRACTION;
    }
}
