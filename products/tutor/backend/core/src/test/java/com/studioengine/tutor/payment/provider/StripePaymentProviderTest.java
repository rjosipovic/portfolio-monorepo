package com.studioengine.tutor.payment.provider;

import com.stripe.exception.ApiException;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.studioengine.tutor.config.PaymentProperties;
import com.studioengine.tutor.errors.exceptions.PaymentProviderException;
import com.studioengine.tutor.errors.exceptions.WebhookDeserializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StripePaymentProviderTest {

    private static final String ZONE_ID = "Europe/Zagreb";
    private static final int YEAR_NOW = 2026;
    private static final int MONTH_NOW = 10;
    private static final int DAY_NOW = 8;
    private static final int HOUR_NOW = 12;
    private static final int MINUTE_NOW = 0;
    private static final String COMPLETED_SESSION_EVENT = "checkout.session.completed";
    private static final String PAYLOAD = "{\"raw\":\"payload\"}";
    private static final String SIGNATURE = "t=1,v1=sig";
    private static final String WEBHOOK_SECRET = "whsec_test";

    @Mock
    private PaymentProperties paymentProperties;
    private StripePaymentProvider stripePaymentProvider;

    @BeforeEach
    void setUp() {
        var zone = ZoneId.of(ZONE_ID);
        var clock = Clock.fixed(LocalDateTime.of(YEAR_NOW, MONTH_NOW, DAY_NOW, HOUR_NOW, MINUTE_NOW).atZone(zone).toInstant(), zone);
        stripePaymentProvider = new StripePaymentProvider(paymentProperties, clock);
    }

    @Nested
    class CreateSession {

        @Test
        void shouldCreateSession() {
            var stripeSessionId = "cs_test_123";
            var stripeRedirectUrl = "https://checkout.stripe.com/c/cs_test_123";
            var successUrl = "https://tutor.studioengine.com/index.html";
            var cancelUrl = "https://tutor.studioengine.com/index.html";
            var stripeSessionExpiry = 35;
            try (var mocked = mockStatic(Session.class)) {
                // given
                var appointmentId = UUID.randomUUID();
                var amount = BigDecimal.valueOf(25L);
                var currency = "EUR";
                var description = "Fakultetska matematika";
                var request = ProviderRequest.builder()
                        .appointmentId(appointmentId)
                        .amount(amount)
                        .currency(currency)
                        .description(description)
                        .build();
                when(paymentProperties.getStripeSuccessUrl()).thenReturn(successUrl);
                when(paymentProperties.getStripeCancelUrl()).thenReturn(cancelUrl);
                when(paymentProperties.getStripeSessionExpiry()).thenReturn(Duration.ofMinutes(stripeSessionExpiry));
                var stripeSession = mock(Session.class);
                when(stripeSession.getId()).thenReturn(stripeSessionId);
                when(stripeSession.getUrl()).thenReturn(stripeRedirectUrl);
                mocked.when(() -> Session.create(any(SessionCreateParams.class))).thenReturn(stripeSession);

                // when
                var result = stripePaymentProvider.createSession(request);

                // then
                var capture = ArgumentCaptor.forClass(SessionCreateParams.class);
                mocked.verify(() -> Session.create(capture.capture()));
                var value = capture.getValue();
                assertThat(value.getMode()).isEqualTo(SessionCreateParams.Mode.PAYMENT);
                assertThat(value.getSuccessUrl()).isEqualTo(successUrl);
                assertThat(value.getCancelUrl()).isEqualTo(cancelUrl);
                assertThat(value.getClientReferenceId()).isEqualTo(appointmentId.toString());
                assertThat(value.getExpiresAt()).isEqualTo(epoch(DAY_NOW, HOUR_NOW, stripeSessionExpiry));
                assertThat(value.getLineItems()).hasSize(1);
                assertThat(value.getLineItems().getFirst().getPriceData().getCurrency()).isEqualTo(currency.toLowerCase());
                assertThat(value.getLineItems().getFirst().getPriceData().getUnitAmount()).isEqualTo(2500L);
                assertThat(value.getLineItems().getFirst().getPriceData().getProductData().getName()).isEqualTo(description);

                assertThat(result.getSessionId()).isEqualTo(stripeSessionId);
                assertThat(result.getRedirectUrl()).isEqualTo(stripeRedirectUrl);
            }
        }

        @Test
        void shouldCreateSessionWithExpiryBelowFloor() {
            var stripeSessionId = "cs_test_123";
            var stripeRedirectUrl = "https://checkout.stripe.com/c/cs_test_123";
            var successUrl = "https://tutor.studioengine.com/index.html";
            var cancelUrl = "https://tutor.studioengine.com/index.html";
            var stripeSessionExpiry = 20;
            try (var mocked = mockStatic(Session.class)) {
                // given
                var appointmentId = UUID.randomUUID();
                var amount = BigDecimal.valueOf(25L);
                var currency = "EUR";
                var description = "Fakultetska matematika";
                var request = ProviderRequest.builder()
                        .appointmentId(appointmentId)
                        .amount(amount)
                        .currency(currency)
                        .description(description)
                        .build();
                when(paymentProperties.getStripeSuccessUrl()).thenReturn(successUrl);
                when(paymentProperties.getStripeCancelUrl()).thenReturn(cancelUrl);
                when(paymentProperties.getStripeSessionExpiry()).thenReturn(Duration.ofMinutes(stripeSessionExpiry));
                var stripeSession = mock(Session.class);
                when(stripeSession.getId()).thenReturn(stripeSessionId);
                when(stripeSession.getUrl()).thenReturn(stripeRedirectUrl);
                mocked.when(() -> Session.create(any(SessionCreateParams.class))).thenReturn(stripeSession);

                // when
                var result = stripePaymentProvider.createSession(request);

                // then
                var capture = ArgumentCaptor.forClass(SessionCreateParams.class);
                mocked.verify(() -> Session.create(capture.capture()));
                var value = capture.getValue();
                assertThat(value.getMode()).isEqualTo(SessionCreateParams.Mode.PAYMENT);
                assertThat(value.getSuccessUrl()).isEqualTo(successUrl);
                assertThat(value.getCancelUrl()).isEqualTo(cancelUrl);
                assertThat(value.getClientReferenceId()).isEqualTo(appointmentId.toString());
                assertThat(value.getExpiresAt()).isEqualTo(epoch(DAY_NOW, HOUR_NOW, 30));
                assertThat(value.getLineItems()).hasSize(1);
                assertThat(value.getLineItems().getFirst().getPriceData().getCurrency()).isEqualTo(currency.toLowerCase());
                assertThat(value.getLineItems().getFirst().getPriceData().getUnitAmount()).isEqualTo(2500L);
                assertThat(value.getLineItems().getFirst().getPriceData().getProductData().getName()).isEqualTo(description);

                assertThat(result.getSessionId()).isEqualTo(stripeSessionId);
                assertThat(result.getRedirectUrl()).isEqualTo(stripeRedirectUrl);
            }
        }

        @Test
        void shouldCreateSessionWithExpiryAboveFloor() {
            var stripeSessionId = "cs_test_123";
            var stripeRedirectUrl = "https://checkout.stripe.com/c/cs_test_123";
            var successUrl = "https://tutor.studioengine.com/index.html";
            var cancelUrl = "https://tutor.studioengine.com/index.html";
            var stripeSessionExpiry = 1445; // 24h + 5min
            try (var mocked = mockStatic(Session.class)) {
                // given
                var appointmentId = UUID.randomUUID();
                var amount = BigDecimal.valueOf(25L);
                var currency = "EUR";
                var description = "Fakultetska matematika";
                var request = ProviderRequest.builder()
                        .appointmentId(appointmentId)
                        .amount(amount)
                        .currency(currency)
                        .description(description)
                        .build();
                when(paymentProperties.getStripeSuccessUrl()).thenReturn(successUrl);
                when(paymentProperties.getStripeCancelUrl()).thenReturn(cancelUrl);
                when(paymentProperties.getStripeSessionExpiry()).thenReturn(Duration.ofMinutes(stripeSessionExpiry));
                var stripeSession = mock(Session.class);
                when(stripeSession.getId()).thenReturn(stripeSessionId);
                when(stripeSession.getUrl()).thenReturn(stripeRedirectUrl);
                mocked.when(() -> Session.create(any(SessionCreateParams.class))).thenReturn(stripeSession);

                // when
                var result = stripePaymentProvider.createSession(request);

                // then
                var capture = ArgumentCaptor.forClass(SessionCreateParams.class);
                mocked.verify(() -> Session.create(capture.capture()));
                var value = capture.getValue();
                assertThat(value.getMode()).isEqualTo(SessionCreateParams.Mode.PAYMENT);
                assertThat(value.getSuccessUrl()).isEqualTo(successUrl);
                assertThat(value.getCancelUrl()).isEqualTo(cancelUrl);
                assertThat(value.getClientReferenceId()).isEqualTo(appointmentId.toString());
                assertThat(value.getExpiresAt()).isEqualTo(epoch(DAY_NOW + 1, HOUR_NOW, MINUTE_NOW));
                assertThat(value.getLineItems()).hasSize(1);
                assertThat(value.getLineItems().getFirst().getPriceData().getCurrency()).isEqualTo(currency.toLowerCase());
                assertThat(value.getLineItems().getFirst().getPriceData().getUnitAmount()).isEqualTo(2500L);
                assertThat(value.getLineItems().getFirst().getPriceData().getProductData().getName()).isEqualTo(description);

                assertThat(result.getSessionId()).isEqualTo(stripeSessionId);
                assertThat(result.getRedirectUrl()).isEqualTo(stripeRedirectUrl);
            }
        }

        private long epoch(int d, int h, int min) {
            return LocalDateTime.of(YEAR_NOW, MONTH_NOW, d, h, min).atZone(ZoneId.of(ZONE_ID)).toEpochSecond();
        }

        @Test
        void shouldThrowPaymentProviderExceptionWhenStripeFails() {
            try (var mocked = mockStatic(Session.class)) {
                // given
                var request = ProviderRequest.builder()
                        .appointmentId(UUID.randomUUID())
                        .amount(new BigDecimal("30.00"))
                        .currency("EUR")
                        .description("Lesson: Math")
                        .build();
                mocked.when(() -> Session.create(any(SessionCreateParams.class)))
                        .thenThrow(new ApiException("stripe down", null, null, 500, null));

                // when / then
                assertThatThrownBy(() -> stripePaymentProvider.createSession(request))
                        .isInstanceOf(PaymentProviderException.class);
            }
        }
    }

    @Nested
    class ProcessCallback {
        @Test
        void shouldFallBackToDeserializeUnsafeWhenObjectNotPresent() throws Exception {
            // given
            var appointmentId = UUID.randomUUID();
            var session = mock(Session.class);
            when(session.getId()).thenReturn("cs_test_123");
            when(session.getClientReferenceId()).thenReturn(appointmentId.toString());
            when(session.getAmountTotal()).thenReturn(4500L);

            var deserializer = mock(EventDataObjectDeserializer.class);
            when(deserializer.getObject()).thenReturn(Optional.empty());
            when(deserializer.deserializeUnsafe()).thenReturn(session);

            var event = mock(Event.class);
            when(event.getType()).thenReturn(COMPLETED_SESSION_EVENT);
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);

            when(paymentProperties.getStripeWebhookSecret()).thenReturn(WEBHOOK_SECRET);

            try (MockedStatic<Webhook> webhook = mockStatic(Webhook.class)) {
                webhook.when(() -> Webhook.constructEvent(PAYLOAD, SIGNATURE, WEBHOOK_SECRET)).thenReturn(event);

                // when
                var result = stripePaymentProvider.processCallback(PAYLOAD, SIGNATURE);

                // then
                assertThat(result).isPresent();
                assertThat(result.get().getSessionId()).isEqualTo("cs_test_123");
                assertThat(result.get().getAppointmentId()).isEqualTo(appointmentId);
                assertThat(result.get().getAmount()).isEqualByComparingTo(new BigDecimal("45.00"));
                verify(deserializer).deserializeUnsafe();
            }
        }

        @Test
        void shouldThrowWhenDeserializeUnsafeFails() throws Exception {
            // given
            var deserializer = mock(EventDataObjectDeserializer.class);
            when(deserializer.getObject()).thenReturn(Optional.empty());
            when(deserializer.deserializeUnsafe())
                    .thenThrow(new EventDataObjectDeserializationException("version skew", PAYLOAD));

            var event = mock(Event.class);
            when(event.getType()).thenReturn(COMPLETED_SESSION_EVENT);
            when(event.getId()).thenReturn("evt_test_123");
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);

            when(paymentProperties.getStripeWebhookSecret()).thenReturn(WEBHOOK_SECRET);

            try (MockedStatic<Webhook> webhook = mockStatic(Webhook.class)) {
                webhook.when(() -> Webhook.constructEvent(PAYLOAD, SIGNATURE, WEBHOOK_SECRET)).thenReturn(event);

                // when / then
                assertThatThrownBy(() -> stripePaymentProvider.processCallback(PAYLOAD, SIGNATURE))
                        .isInstanceOf(WebhookDeserializationException.class);
            }
        }

        @Test
        void shouldUseObjectDirectlyWithoutFallbackWhenPresent() throws Exception {
            // given
            var appointmentId = UUID.randomUUID();
            var session = mock(Session.class);
            when(session.getId()).thenReturn("cs_test_456");
            when(session.getClientReferenceId()).thenReturn(appointmentId.toString());
            when(session.getAmountTotal()).thenReturn(1000L);

            var deserializer = mock(EventDataObjectDeserializer.class);
            when(deserializer.getObject()).thenReturn(Optional.of(session));

            var event = mock(Event.class);
            when(event.getType()).thenReturn(COMPLETED_SESSION_EVENT);
            when(event.getDataObjectDeserializer()).thenReturn(deserializer);

            when(paymentProperties.getStripeWebhookSecret()).thenReturn(WEBHOOK_SECRET);

            try (MockedStatic<Webhook> webhook = mockStatic(Webhook.class)) {
                webhook.when(() -> Webhook.constructEvent(PAYLOAD, SIGNATURE, WEBHOOK_SECRET)).thenReturn(event);

                // when
                var result = stripePaymentProvider.processCallback(PAYLOAD, SIGNATURE);

                // then
                assertThat(result).isPresent();
                assertThat(result.get().getSessionId()).isEqualTo("cs_test_456");
                assertThat(result.get().getAppointmentId()).isEqualTo(appointmentId);
                assertThat(result.get().getAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
                verify(deserializer, never()).deserializeUnsafe();
            }
        }

        @Test
        void shouldReturnEmptyForNonCompletedEventType() throws Exception {
            // given
            var event = mock(Event.class);
            when(event.getType()).thenReturn("payment_intent.created");

            when(paymentProperties.getStripeWebhookSecret()).thenReturn(WEBHOOK_SECRET);

            try (MockedStatic<Webhook> webhook = mockStatic(Webhook.class)) {
                webhook.when(() -> Webhook.constructEvent(PAYLOAD, SIGNATURE, WEBHOOK_SECRET)).thenReturn(event);

                // when
                var result = stripePaymentProvider.processCallback(PAYLOAD, SIGNATURE);

                // then
                assertThat(result).isEmpty();
            }
        }
    }
}
