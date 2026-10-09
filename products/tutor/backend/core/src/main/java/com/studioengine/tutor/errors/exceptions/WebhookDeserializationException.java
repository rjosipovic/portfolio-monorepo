package com.studioengine.tutor.errors.exceptions;

import com.studioengine.tutor.errors.TutorEngineException;

import static com.studioengine.tutor.errors.ErrorCode.WEBHOOK_DESERIALIZATION_FAILED;

/** Thrown when a Stripe webhook request fails deserialization. */
public class WebhookDeserializationException extends TutorEngineException {

    public WebhookDeserializationException(String detail) {
        super(WEBHOOK_DESERIALIZATION_FAILED, detail);
    }
}
