package com.studioengine.tutor.errors.exceptions;

import com.studioengine.tutor.errors.ErrorCode;
import com.studioengine.tutor.errors.TutorEngineException;

/** Throw when tutor attempts to create slot not on the hour (min,sec must be = 0) */
public class InvalidSlotTimeException extends TutorEngineException {

    public InvalidSlotTimeException(String detail) {
        super(ErrorCode.SLOT_NOT_ON_THE_HOUR, detail);
    }
}
