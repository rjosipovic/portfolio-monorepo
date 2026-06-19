package com.studioengine.tutor.checkout;

import com.studioengine.tutor.dataaccess.entities.ServiceCategory;
import com.studioengine.tutor.dataaccess.entities.Student;
import com.studioengine.tutor.dataaccess.entities.TimeSlot;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class CheckoutContext {
    TimeSlot slot;
    ServiceCategory category;
    Student student;
    BigDecimal originalPrice;
    BigDecimal finalPrice;
    String sessionNotes;
    PaymentMethodChoice paymentMethodChoice;
    Checkout.BenefitApplied benefitApplied;
}
