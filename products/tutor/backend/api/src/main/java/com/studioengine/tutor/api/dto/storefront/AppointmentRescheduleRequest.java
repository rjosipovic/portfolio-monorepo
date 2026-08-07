package com.studioengine.tutor.api.dto.storefront;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = AppointmentRescheduleRequest.AppointmentRescheduleRequestBuilder.class)
public class AppointmentRescheduleRequest {

    @NotBlank
    String token;

    @JsonPOJOBuilder(withPrefix = "")
    public static class AppointmentRescheduleRequestBuilder {}
}
