package com.playground.user_manager.user.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.UUID;

public class UuidListValidator implements ConstraintValidator<ValidUuidList, List<String>> {

    @Override
    public boolean isValid(List<String> value, ConstraintValidatorContext context) {
        if (value == null) return true;
        for (String id : value) {
            try {
                UUID.fromString(id);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }
}
