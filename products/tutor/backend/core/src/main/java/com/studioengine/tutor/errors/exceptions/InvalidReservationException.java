package com.studioengine.tutor.errors.exceptions;

import com.studioengine.tutor.errors.ErrorCode;
import com.studioengine.tutor.errors.TutorEngineException;

/** Thrown when checkout references a reservation that has expired or is not in RESERVED state. */
public class InvalidReservationException extends TutorEngineException {

    public InvalidReservationException(String detail) {
        super(ErrorCode.INVALID_RESERVATION, detail);
    }
}
