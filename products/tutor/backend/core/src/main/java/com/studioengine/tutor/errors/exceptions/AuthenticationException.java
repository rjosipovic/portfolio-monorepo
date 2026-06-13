package com.studioengine.tutor.errors.exceptions;

import com.studioengine.tutor.errors.ErrorCode;
import com.studioengine.tutor.errors.TutorEngineException;

/** Thrown when a request to a protected endpoint lacks a valid JWT or the JWT has expired. */
public class AuthenticationException extends TutorEngineException {

    public AuthenticationException(String detail) {
        super(ErrorCode.AUTHENTICATION_FAILED, detail);
    }
}