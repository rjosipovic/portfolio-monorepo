package com.studioengine.tutor.api.dto.storefront;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = CheckoutResponse.CheckoutResponseBuilder.class)
public class CheckoutResponse {

    UUID appointmentId;
    String status;
    String stripeRedirectUrl;
    String message;
    BenefitAppliedResponse benefitApplied;

    @Value
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonDeserialize(builder = BenefitAppliedResponse.BenefitAppliedResponseBuilder.class)
    public static class BenefitAppliedResponse {
        String type;
        BigDecimal originalPrice;
        BigDecimal finalPrice;

        @JsonPOJOBuilder(withPrefix = "")
        public static class BenefitAppliedResponseBuilder {}
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class CheckoutResponseBuilder {}
}
