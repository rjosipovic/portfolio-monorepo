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
public class ProviderRequest {

    UUID appointmentId;
    BigDecimal amount;
    String currency;
    String description;
    String successUrl;
    String cancelUrl;
}

