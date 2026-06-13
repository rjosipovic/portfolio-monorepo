package com.studioengine.tutor.errors.exceptions;

import com.studioengine.tutor.errors.ErrorCode;
import com.studioengine.tutor.errors.TutorEngineException;

/** Thrown when OTP verification fails due to invalid code, expiration, or account lockout. */
public class OtpVerificationException extends TutorEngineException {

    public OtpVerificationException(String detail) {
        super(ErrorCode.OTP_VERIFICATION_FAILED, detail);
    }
}
