package com.studioengine.tutor.api.dto.summary;

import com.studioengine.tutor.api.dto.TimeSlotResponse;
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
public class TimeSlotSummary {

    UUID id;
    LocalDate date;
    LocalTime startTime;

    @JsonPOJOBuilder(withPrefix = "")
    public static class TimeSlotSummaryBuilder{}
}
