package com.studioengine.tutor.errors.exceptions;

import com.studioengine.tutor.errors.ErrorCode;
import com.studioengine.tutor.errors.TutorEngineException;

/** Thrown when trying to create student with existing email */
public class EmailAlreadyInUseException extends TutorEngineException {

    public EmailAlreadyInUseException(String detail) {
        super(ErrorCode.EMAIL_ALREADY_IN_USE, detail);
    }
}
