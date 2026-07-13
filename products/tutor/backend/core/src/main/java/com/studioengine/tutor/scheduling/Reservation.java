package com.studioengine.tutor.scheduling;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Reservation {

    UUID timeSlotId;
    OffsetDateTime expiresAt;
}
