package com.playground.challenge_manager.challenge.services.impl.calculation;

import com.playground.challenge_manager.challenge.services.interfaces.OperationCalculator;
import com.playground.challenge_manager.challenge.services.model.OperationType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MultiplicationCalculator implements OperationCalculator {

    @Override
    public int calculate(List<Integer> operands) {
        return operands.stream().reduce(1, (a, b) -> a * b);
    }

    @Override
    public OperationType getOperationType() {
        return OperationType.MULTIPLICATION;
    }
}
