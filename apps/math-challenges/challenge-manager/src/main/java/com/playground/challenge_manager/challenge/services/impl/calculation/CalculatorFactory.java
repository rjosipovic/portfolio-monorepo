package com.playground.challenge_manager.challenge.services.impl.calculation;

import com.playground.challenge_manager.challenge.services.interfaces.OperationCalculator;
import com.playground.challenge_manager.challenge.services.model.OperationType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CalculatorFactory {

    private final Map<OperationType, OperationCalculator> calculators = new HashMap<>();
    private final List<OperationCalculator> calculatorsList;

    @PostConstruct
    private void init() {
        calculatorsList.forEach(calculator -> calculators.put(calculator.getOperationType(), calculator));
    }

    public OperationCalculator getCalculator(OperationType operationType) {
        var calculator = calculators.get(operationType);
        if (Objects.isNull(calculator)) {
            throw new IllegalArgumentException("No calculator found for type: " + operationType);
        }
        return calculator;
    }
}
