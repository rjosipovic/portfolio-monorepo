package com.studioengine.tutor.scheduling;

import com.studioengine.tutor.dataaccess.enums.TimeSlotState;
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
public class CreatedSlot {

    UUID id;
    LocalDate date;
    LocalTime startTime;
    LocalTime endTime;
    TimeSlotState state;
}