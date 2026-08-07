package com.studioengine.tutor.api.dto.storefront;

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
@JsonDeserialize(builder = AppointmentDetailsResponse.AppointmentDetailsResponseBuilder.class)
public class AppointmentCancellationResponse {

    UUID appointmentId;
    String message;

    @JsonPOJOBuilder(withPrefix = "")
    public static class AppointmentDetailsResponseBuilder {}
}
