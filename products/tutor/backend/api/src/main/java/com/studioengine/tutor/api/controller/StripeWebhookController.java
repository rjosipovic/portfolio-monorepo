package com.studioengine.tutor.api.controller;

import com.studioengine.tutor.payment.PaymentProvider;
import com.studioengine.tutor.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private final PaymentProvider paymentProvider;
    private final PaymentService paymentService;

    @PostMapping("/stripe")
    public ResponseEntity<Void> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        log.info("POST /webhooks/stripe received");

        var result = paymentProvider.processCallback(payload, signature);
        result.ifPresent(paymentService::handleStripeWebhookConfirmation);
        return ResponseEntity.ok().build();
    }
}
