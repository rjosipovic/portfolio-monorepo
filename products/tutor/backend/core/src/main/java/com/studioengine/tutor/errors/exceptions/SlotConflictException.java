package com.studioengine.tutor.errors.exceptions;

import com.studioengine.tutor.errors.ErrorCode;
import com.studioengine.tutor.errors.TutorEngineException;

/** Thrown when a guest attempts to reserve a time slot that is already reserved by another guest. */
public class SlotConflictException extends TutorEngineException {

    public SlotConflictException(String detail) {
        super(ErrorCode.SLOT_CONFLICT, detail);
    }
}

