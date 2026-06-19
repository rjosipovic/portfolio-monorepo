package com.studioengine.tutor.checkout;

import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Checkout {

    UUID appointmentId;
    AppointmentState status;
    String stripeRedirectUrl;     // null if not Stripe
    String message;               // human-readable status
    BenefitApplied benefitApplied; // null if no benefit

    @Value
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class BenefitApplied {
        String type;
        BigDecimal originalPrice;
        BigDecimal finalPrice;
    }
}