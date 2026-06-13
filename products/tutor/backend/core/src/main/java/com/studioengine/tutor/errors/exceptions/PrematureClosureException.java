package com.studioengine.tutor.errors.exceptions;

import com.studioengine.tutor.errors.ErrorCode;
import com.studioengine.tutor.errors.TutorEngineException;

/** Thrown when a tutor attempts to close an appointment as COMPLETED or NO_SHOW before the scheduled end time. */
public class PrematureClosureException extends TutorEngineException {

    public PrematureClosureException(String detail) {
        super(ErrorCode.PREMATURE_CLOSURE, detail);
    }
}
