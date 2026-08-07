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
@JsonDeserialize(builder = AppointmentCancellationRequest.AppointmentCancellationRequestBuilder.class)
public class AppointmentCancellationRequest {

    @NotBlank
    String token;

    @JsonPOJOBuilder(withPrefix = "")
    public static class AppointmentCancellationRequestBuilder {}
}
