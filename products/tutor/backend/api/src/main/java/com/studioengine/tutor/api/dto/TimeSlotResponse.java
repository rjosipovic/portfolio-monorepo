package com.studioengine.tutor.api.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = TimeSlotResponse.TimeSlotResponseBuilder.class)
public class TimeSlotResponse {

    UUID id;
    LocalDate date;
    LocalTime startTime;
    LocalTime endTime;

    @JsonPOJOBuilder(withPrefix = "")
    public static class TimeSlotResponseBuilder {
    }
}

