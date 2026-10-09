package com.studioengine.tutor.payment.provider;

import com.stripe.Stripe;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.studioengine.tutor.config.PaymentProperties;
import com.studioengine.tutor.errors.exceptions.PaymentProviderException;
import com.studioengine.tutor.errors.exceptions.WebhookDeserializationException;
import com.studioengine.tutor.errors.exceptions.WebhookVerificationException;
import com.studioengine.tutor.payment.PaymentProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class StripePaymentProvider implements PaymentProvider {

    private static final Duration STRIPE_MIN_EXPIRY = Duration.ofMinutes(30);
    private static final Duration STRIPE_MAX_EXPIRY = Duration.ofHours(24);
    private static final String COMPLETED_SESSION_EVENT = "checkout.session.completed";

    private final PaymentProperties paymentProperties;
    private final Clock clock;

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
            throw new PaymentProviderException("Stripe session creation failed for appointment %s".formatted(request.getAppointmentId()));
        }
    }

    private Session buildSession(ProviderRequest request) throws StripeException {
        var params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(paymentProperties.getStripeSuccessUrl())
                .setCancelUrl(paymentProperties.getStripeCancelUrl())
                .setClientReferenceId(request.getAppointmentId().toString())
                .setExpiresAt(stripeExpiresAt())
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
        } catch (SignatureVerificationException e) {
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
            var session = deserializeSession(event);
            return Optional.of(ProviderResult.builder()
                    .sessionId(session.getId())
                    .appointmentId(UUID.fromString(session.getClientReferenceId()))
                    .amount(BigDecimal.valueOf(session.getAmountTotal()).movePointLeft(2))
                    .build());
        }
    }

    private Session deserializeSession(Event event) {
        var deserializer = event.getDataObjectDeserializer();
        var deserialized = deserializer.getObject();
        if (deserialized.isPresent()) {
            return (Session) deserialized.get();
        }
        // API-version skew between the event payload and stripe-java: fall back to
        // forced deserialization. We only read the stable fields (id, clientReferenceId, amountTotal)
        try {
            log.warn("Stripe event {} not deserializable at pinned API version; falling back to deserializeUnsafe", event.getId());
            return (Session) deserializer.deserializeUnsafe();
        } catch (EventDataObjectDeserializationException ex) {
            throw new WebhookDeserializationException("Failed to deserialize Stripe checkout session for event %s".formatted(event.getId()));
        }
    }

    /**
     * Calculates the Stripe session expiry timestamp in epoch seconds.
     * Clamps the requested expiry to Stripe's allowed range [now+30min, now+24h].
     * Intended as misconfiguration guard: if someone sets stripe-session-expiry below 30min or above 24h
     *
     * @return epoch seconds for the clamped expiry time
     * @see <a href="https://stripe.com/docs/api/checkout/sessions/create#create_checkout_session-expires_at">Stripe docs</a>
     */
    private long stripeExpiresAt() {
        var now = OffsetDateTime.now(clock);
        var requested = now.plus(paymentProperties.getStripeSessionExpiry());
        var min = now.plus(STRIPE_MIN_EXPIRY);
        var max = now.plus(STRIPE_MAX_EXPIRY);
        var clamped = clamp(requested, min, max);
        return clamped.toEpochSecond();
    }

    private static OffsetDateTime clamp(OffsetDateTime value, OffsetDateTime min, OffsetDateTime max) {
        if (value.isBefore(min)) return min;
        if (value.isAfter(max)) return max;
        return value;
    }

    private long toStripeAmount(BigDecimal amount) {
        return amount.movePointRight(2).longValue();
    }
}
