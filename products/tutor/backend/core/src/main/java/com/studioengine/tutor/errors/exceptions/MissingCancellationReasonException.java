package com.studioengine.tutor.errors.exceptions;

import com.studioengine.tutor.errors.ErrorCode;
import com.studioengine.tutor.errors.TutorEngineException;

/** Thrown when a tutor cancels an appointment without providing a cancellation reason. */
public class MissingCancellationReasonException extends TutorEngineException {

    public MissingCancellationReasonException(String detail) {
        super(ErrorCode.MISSING_CANCELLATION_REASON, detail);
    }
}
