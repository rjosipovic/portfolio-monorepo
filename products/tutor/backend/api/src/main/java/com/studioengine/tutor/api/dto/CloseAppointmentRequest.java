package com.studioengine.tutor.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = CloseAppointmentRequest.CloseAppointmentRequestBuilder.class)
public class CloseAppointmentRequest {

    @NotNull
    Outcome outcome;

    boolean sendFollowup;

    public enum Outcome {
        COMPLETED,
        NO_SHOW
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class CloseAppointmentRequestBuilder {}
}
