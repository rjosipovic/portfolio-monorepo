package com.studioengine.tutor.errors.exceptions;

import com.studioengine.tutor.errors.ErrorCode;
import com.studioengine.tutor.errors.TutorEngineException;

/** Thrown when a student attempts self-cancellation or reschedule on a PRE_BOOKED appointment. */
public class PreBookedSelfServiceException extends TutorEngineException {

    public PreBookedSelfServiceException(String detail) {
        super(ErrorCode.PRE_BOOKED_SELF_SERVICE, detail);
    }
}
