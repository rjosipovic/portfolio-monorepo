package com.studioengine.tutor.booking;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DirectBookingCommand {

    UUID timeSlotId;
    UUID studentId;
    UUID serviceCategoryId;
}
