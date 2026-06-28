package com.studioengine.tutor.api.dto.summary;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = AppointmentSummary.AppointmentSummaryBuilder.class)
public class AppointmentSummary {
    UUID id;
    String state;

    @JsonPOJOBuilder(withPrefix = "")
    public static class AppointmentSummaryBuilder {}
}

