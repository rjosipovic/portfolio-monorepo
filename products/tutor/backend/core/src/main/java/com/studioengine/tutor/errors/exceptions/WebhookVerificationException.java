package com.studioengine.tutor.errors.exceptions;

import com.studioengine.tutor.errors.ErrorCode;
import com.studioengine.tutor.errors.TutorEngineException;

/** Thrown when a Stripe webhook request fails Stripe-Signature header verification. */
public class WebhookVerificationException extends TutorEngineException {

    public WebhookVerificationException(String detail) {
        super(ErrorCode.WEBHOOK_VERIFICATION_FAILED, detail);
    }
}