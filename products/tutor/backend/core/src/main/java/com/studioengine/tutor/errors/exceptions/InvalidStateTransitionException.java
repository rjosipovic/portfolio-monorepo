package com.studioengine.tutor.errors.exceptions;

import com.studioengine.tutor.errors.ErrorCode;
import com.studioengine.tutor.errors.TutorEngineException;

/** Thrown when an attempted state transition violates the TimeSlot or Appointment state machine rules. */
public class InvalidStateTransitionException extends TutorEngineException {

    public InvalidStateTransitionException(String detail) {
        super(ErrorCode.INVALID_STATE_TRANSITION, detail);
    }
}

