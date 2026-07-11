package com.studioengine.tutor.api.dto.student;

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
@JsonDeserialize(builder = AppointmentHistoryResponse.AppointmentHistoryResponseBuilder.class)
public class AppointmentHistoryResponse {

    UUID appointmentId;
    LocalDate date;
    LocalTime startTime;
    String serviceCategoryName;
    String state;

    @JsonPOJOBuilder(withPrefix = "")
    public static class AppointmentHistoryResponseBuilder {}
}
