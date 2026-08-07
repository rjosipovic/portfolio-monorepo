package com.studioengine.tutor.api.dto.storefront;

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
@JsonDeserialize(builder = AppointmentDetailsResponse.AppointmentDetailsResponseBuilder.class)
public class AppointmentDetailsResponse {

    UUID appointmentId;
    String studentName;
    String serviceCategoryName;
    LocalDate date;
    LocalTime startTime;
    boolean deadlineMissed;

    @JsonPOJOBuilder(withPrefix = "")
    public static class AppointmentDetailsResponseBuilder {}
}
