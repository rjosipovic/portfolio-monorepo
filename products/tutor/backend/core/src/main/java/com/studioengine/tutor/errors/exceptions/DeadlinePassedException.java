package com.studioengine.tutor.errors.exceptions;

import com.studioengine.tutor.errors.ErrorCode;
import com.studioengine.tutor.errors.TutorEngineException;

/** Thrown when a student attempts self-cancellation or reschedule after the configured deadline (default 24h before start). */
public class DeadlinePassedException extends TutorEngineException {

    public DeadlinePassedException(String detail) {
        super(ErrorCode.DEADLINE_PASSED, detail);
    }
}
