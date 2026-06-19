package com.studioengine.tutor.checkout;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CheckoutCommand {

    UUID reservedSlotId;
    UUID serviceCategoryId;
    String guestName;
    String guestEmail;
    String guestPhone;
    String sessionNotes;
    PaymentMethodChoice paymentMethod;
}
