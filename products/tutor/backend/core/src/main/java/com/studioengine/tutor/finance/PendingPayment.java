package com.studioengine.tutor.finance;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PendingPayment {

    UUID appointmentId;
    String studentName;
    BigDecimal amount;
    OffsetDateTime createdAt;
}
