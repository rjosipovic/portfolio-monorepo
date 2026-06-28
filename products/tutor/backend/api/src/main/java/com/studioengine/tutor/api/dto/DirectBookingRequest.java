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
@JsonDeserialize(builder = DirectBookingRequest.DirectBookingRequestBuilder.class)
public class DirectBookingRequest {

    @NotNull
    UUID timeSlotId;

    @NotNull
    UUID studentId;

    @NotNull
    UUID serviceCategoryId;

    @JsonPOJOBuilder(withPrefix = "")
    public static class DirectBookingRequestBuilder {}
}
