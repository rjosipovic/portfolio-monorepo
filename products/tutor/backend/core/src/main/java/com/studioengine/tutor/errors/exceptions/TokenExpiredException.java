package com.studioengine.tutor.errors.exceptions;

import com.studioengine.tutor.errors.ErrorCode;
import com.studioengine.tutor.errors.TutorEngineException;

/** Thrown when a cancellation or reschedule token has already been used or has passed its expiration time. */
public class TokenExpiredException extends TutorEngineException {

    public TokenExpiredException(String detail) {
        super(ErrorCode.TOKEN_EXPIRED, detail);
    }
}

