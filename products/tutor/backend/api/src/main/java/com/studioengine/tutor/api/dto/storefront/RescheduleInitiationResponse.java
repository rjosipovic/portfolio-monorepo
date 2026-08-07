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
@JsonDeserialize(builder = RescheduleInitiationResponse.RescheduleInitiationResponseBuilder.class)
public class RescheduleInitiationResponse {

    UUID originalAppointmentId;
    String rescheduleToken;
    String redirectUrl;

    @JsonPOJOBuilder(withPrefix = "")
    public static class RescheduleInitiationResponseBuilder {}
}
