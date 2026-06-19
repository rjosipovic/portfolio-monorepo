package com.studioengine.tutor.payment;

import com.studioengine.tutor.payment.provider.ProviderResult;

public interface PaymentService {

    PaymentInitiation initPayment(PaymentCommand ctx);

    void handleStripeWebhookConfirmation(ProviderResult result);
}
