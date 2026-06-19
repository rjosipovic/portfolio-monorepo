package com.studioengine.tutor.payment.provider;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProviderResult {

    String sessionId;
    UUID appointmentId;
    PaymentOutcome outcome;
    BigDecimal amount;

    public enum PaymentOutcome {
        SUCCESS,
        FAILED
    }
}

