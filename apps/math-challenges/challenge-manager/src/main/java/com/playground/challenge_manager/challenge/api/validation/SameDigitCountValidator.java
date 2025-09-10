package com.playground.challenge_manager.challenge.api.validation;

import com.playground.challenge_manager.challenge.api.dto.ChallengeAttemptDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SameDigitCountValidator implements ConstraintValidator<SameDigitCount, ChallengeAttemptDTO> {

    @Override
    public boolean isValid(ChallengeAttemptDTO value, ConstraintValidatorContext context) {
        if (value.getFirstNumber() == null || value.getSecondNumber() == null) {
            return true; // let @NotNull handle this
        }
        return getDigitCount(value.getFirstNumber()) == getDigitCount(value.getSecondNumber());
    }

    private int getDigitCount(int number) {
        if (number == 0) return 1;
        return (int) Math.log10(Math.abs(number)) + 1;
    }
}
