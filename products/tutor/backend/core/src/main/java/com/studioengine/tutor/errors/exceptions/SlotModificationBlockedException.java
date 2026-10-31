package com.studioengine.tutor.errors.exceptions;

import com.studioengine.tutor.errors.ErrorCode;
import com.studioengine.tutor.errors.TutorEngineException;

/** Thrown when a tutor attempts to withdraw or delete a slot that has an active appointment. */
public class SlotModificationBlockedException extends TutorEngineException {

    public SlotModificationBlockedException(String detail) {
        super(ErrorCode.SLOT_MODIFICATION_BLOCKED, detail);
    }
}
