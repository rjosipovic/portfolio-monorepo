package com.playground.user_manager.user.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UuidListValidator.class)
public @interface ValidUuidList {
    String message() default "Invalid UUID in list";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
