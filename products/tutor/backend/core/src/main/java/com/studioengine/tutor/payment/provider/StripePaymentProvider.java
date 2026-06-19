package com.studioengine.tutor.payment.provider;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.studioengine.tutor.config.PaymentProperties;
import com.studioengine.tutor.errors.exceptions.WebhookVerificationException;
import com.studioengine.tutor.payment.PaymentProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class StripePaymentProvider implements PaymentProvider {

    private static final String COMPLETED_SESSION_EVENT = "checkout.session.completed";

    private final PaymentProperties paymentProperties;

    @PostConstruct
    void init() {
        Stripe.apiKey = paymentProperties.getStripeSecretKey();
    }

    @Override
    public ProviderSession createSession(ProviderRequest request) {
        try {
            var session = buildSession(request);
            return ProviderSession.builder()
                    .sessionId(session.getId())
                    .redirectUrl(session.getUrl())
                    .build();
        } catch (StripeException e) {
            log.error("Failed to create Stripe session for appointment {}: {}", request.getAppointmentId(), e.getMessage());
            throw new RuntimeException("Stripe session creation failed", e);
        }
    }

    private Session buildSession(ProviderRequest request) throws StripeException {
        var params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(paymentProperties.getStripeSuccessUrl())
                .setCancelUrl(paymentProperties.getStripeCancelUrl())
                .setClientReferenceId(request.getAppointmentId().toString())
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(request.getCurrency().toLowerCase())
                                .setUnitAmount(toStripeAmount(request.getAmount()))
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(request.getDescription())
                                        .build())
                                .build())
                        .build())
                .build();
        return Session.create(params);
    }

    @Override
    public Optional<ProviderResult> processCallback(String payload, String signature) {
        try {
            return handleStripeEvent(payload, signature);
        } catch (Exception e) {
            log.error("Failed to process Stripe webhook: {}", e.getMessage());
            throw new WebhookVerificationException("Stripe webhook processing failed");
        }
    }

    private Optional<ProviderResult> handleStripeEvent(String payload, String signature) throws SignatureVerificationException {
        var event = Webhook.constructEvent(payload, signature, paymentProperties.getStripeWebhookSecret());
        if (!COMPLETED_SESSION_EVENT.equals(event.getType())) {
            log.info("Ignoring Stripe event type: {}", event.getType());
            return Optional.empty();
        } else {
            var session = (Session) event.getDataObjectDeserializer().getObject().orElseThrow();
            return Optional.of(ProviderResult.builder()
                    .sessionId(session.getId())
                    .appointmentId(UUID.fromString(session.getClientReferenceId()))
                    .outcome(ProviderResult.PaymentOutcome.SUCCESS)
                    .amount(BigDecimal.valueOf(session.getAmountTotal()).movePointLeft(2))
                    .build());
        }
    }

    private long toStripeAmount(BigDecimal amount) {
        return amount.movePointRight(2).longValue();
    }
}
