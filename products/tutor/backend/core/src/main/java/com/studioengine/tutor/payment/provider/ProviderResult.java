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
    String paymentIntentId;
    UUID appointmentId;
    BigDecimal amount;
}

