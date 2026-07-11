package com.studioengine.tutor.api.dto.storefront;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = ReservationResponse.ReservationResponseBuilder.class)
public class ReservationResponse {

    UUID timeSlotId;
    OffsetDateTime expiresAt;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ReservationResponseBuilder {
    }
}

