package com.studioengine.tutor.api.dto;

import jakarta.validation.constraints.NotNull;
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
@JsonDeserialize(builder = ReservationRequest.ReservationRequestBuilder.class)
public class ReservationRequest {

    @NotNull
    UUID timeSlotId;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ReservationRequestBuilder {
    }
}
