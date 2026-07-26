package com.studioengine.tutor.api.dto.finance;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = PendingPaymentResponse.PendingPaymentResponseBuilder.class)
public class PendingPaymentResponse {

    UUID appointmentId;
    String studentName;
    BigDecimal amount;
    OffsetDateTime createdAt;

    @JsonPOJOBuilder(withPrefix = "")
    public static class PendingPaymentResponseBuilder {}
}
