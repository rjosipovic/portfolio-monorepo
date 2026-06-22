package com.studioengine.tutor.scheduling;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateSlotsCommand {

    List<SlotDefinition> slots;

    @Value
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SlotDefinition {
        LocalDate date;
        LocalTime startTime;
    }
}
