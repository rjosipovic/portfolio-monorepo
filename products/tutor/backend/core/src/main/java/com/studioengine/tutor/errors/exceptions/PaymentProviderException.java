package com.studioengine.tutor.errors.exceptions;

import com.studioengine.tutor.errors.TutorEngineException;
import static com.studioengine.tutor.errors.ErrorCode.PAYMENT_PROVIDER_ERROR;

/** Thrown when the payment provider (Stripe) fails to create a checkout session. */
public class PaymentProviderException extends TutorEngineException {

    public PaymentProviderException(String detail) {
        super(PAYMENT_PROVIDER_ERROR, detail);
    }
}
