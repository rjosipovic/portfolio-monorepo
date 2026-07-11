package com.studioengine.tutor.api.dto.appointment;

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
@JsonDeserialize(builder = CancelAppointmentRequest.CancelAppointmentRequestBuilder.class)
public class CancelAppointmentRequest {

    @NotBlank
    String reason;

    @JsonPOJOBuilder(withPrefix = "")
    public static class CancelAppointmentRequestBuilder {}
}
