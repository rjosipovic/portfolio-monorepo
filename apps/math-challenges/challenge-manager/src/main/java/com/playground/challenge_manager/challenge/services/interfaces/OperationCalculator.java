package com.playground.challenge_manager.challenge.services.interfaces;

import com.playground.challenge_manager.challenge.services.model.OperationType;
import java.util.List;

/**
 * Defines the contract for a component that can perform a specific mathematical operation.
 * This is the "Strategy" interface in the Strategy pattern.
 */
public interface OperationCalculator {
    /**
     * Calculates the result for a given list of operands.
     * @param operands The numbers to perform the calculation on.
     * @return The integer result of the calculation.
     */
    int calculate(List<Integer> operands);

    /**
     * Specifies which OperationType this calculator handles.
     * This is used by the factory to map the correct implementation.
     * @return The OperationType supported by this calculator.
     */
    OperationType getOperationType();
}