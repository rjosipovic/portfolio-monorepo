package com.studioengine.tutor.scheduling;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AvailableSlot {

    UUID id;
    LocalDate date;
    LocalTime startTime;
    LocalTime endTime;
}

