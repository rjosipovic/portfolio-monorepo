package com.studioengine.tutor.payment;

import com.studioengine.tutor.checkout.PaymentMethodChoice;
import com.studioengine.tutor.dataaccess.entities.Appointment;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentCommand {

    Appointment appointment;
    PaymentMethodChoice paymentMethod;
}
