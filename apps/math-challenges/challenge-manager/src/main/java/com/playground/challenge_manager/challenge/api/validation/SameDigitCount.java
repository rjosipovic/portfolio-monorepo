package com.playground.challenge_manager.challenge.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({java.lang.annotation.ElementType.TYPE})
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SameDigitCountValidator.class)
public @interface SameDigitCount {
    String message() default "Numbers must have the same digit count";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
