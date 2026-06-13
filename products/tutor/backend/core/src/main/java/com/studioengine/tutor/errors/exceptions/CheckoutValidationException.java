package com.studioengine.tutor.errors.exceptions;

import com.studioengine.tutor.errors.ErrorCode;
import com.studioengine.tutor.errors.TutorEngineException;

/** Thrown when checkout input fails validation (missing fields, invalid email, etc.). */
public class CheckoutValidationException extends TutorEngineException {

    public CheckoutValidationException(String detail) {
        super(ErrorCode.CHECKOUT_VALIDATION_FAILED, detail);
    }
}