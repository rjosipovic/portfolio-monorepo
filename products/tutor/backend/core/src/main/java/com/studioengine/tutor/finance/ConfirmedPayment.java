package com.studioengine.tutor.finance;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfirmedPayment {

    UUID appointmentId;
    String state;
    OffsetDateTime confirmedAt;
}
