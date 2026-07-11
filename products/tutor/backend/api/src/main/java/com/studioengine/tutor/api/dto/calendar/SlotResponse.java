package com.studioengine.tutor.api.dto.calendar;

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
@JsonDeserialize(builder = SlotResponse.SlotResponseBuilder.class)
public class SlotResponse {

    UUID id;
    LocalDate date;
    LocalTime startTime;
    LocalTime endTime;
    String state;

    @JsonPOJOBuilder(withPrefix = "")
    public static class SlotResponseBuilder {}
}
