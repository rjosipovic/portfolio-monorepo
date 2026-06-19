package com.studioengine.tutor.payment;

import com.studioengine.tutor.payment.provider.ProviderRequest;
import com.studioengine.tutor.payment.provider.ProviderResult;
import com.studioengine.tutor.payment.provider.ProviderSession;

import java.util.Optional;

public interface PaymentProvider {

    ProviderSession createSession(ProviderRequest request);
    Optional<ProviderResult> processCallback(String payload, String signature);
}