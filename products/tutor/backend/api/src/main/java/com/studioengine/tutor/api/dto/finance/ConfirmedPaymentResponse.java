package com.studioengine.tutor.api.dto.finance;

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
@JsonDeserialize(builder = ConfirmedPaymentResponse.ConfirmedPaymentResponseBuilder.class)
public class ConfirmedPaymentResponse {

    UUID appointmentId;
    String state;
    OffsetDateTime confirmedAt;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ConfirmedPaymentResponseBuilder {}
}
