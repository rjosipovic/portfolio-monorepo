package com.studioengine.tutor.payment;

import com.studioengine.tutor.checkout.PaymentMethodChoice;
import com.studioengine.tutor.config.BrandProperties;
import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.PaymentRecord;
import com.studioengine.tutor.dataaccess.entities.ServiceCategory;
import com.studioengine.tutor.dataaccess.entities.Student;
import com.studioengine.tutor.dataaccess.entities.TimeSlot;
import com.studioengine.tutor.dataaccess.enums.AppointmentOrigin;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.dataaccess.enums.PaymentMethod;
import com.studioengine.tutor.dataaccess.enums.TimeSlotState;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import com.studioengine.tutor.dataaccess.repositories.PaymentRecordRepository;
import com.studioengine.tutor.dataaccess.repositories.TimeSlotRepository;
import com.studioengine.tutor.email.EmailService;
import com.studioengine.tutor.errors.exceptions.WebhookVerificationException;
import com.studioengine.tutor.payment.provider.ProviderRequest;
import com.studioengine.tutor.payment.provider.ProviderResult;
import com.studioengine.tutor.payment.provider.ProviderSession;
import com.studioengine.tutor.scheduling.AppointmentStateMachine;
import com.studioengine.tutor.scheduling.TimeSlotStateMachine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentProvider paymentProvider;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private TimeSlotRepository timeSlotRepository;
    @Mock
    private AppointmentStateMachine appointmentStateMachine;
    @Mock
    private TimeSlotStateMachine timeSlotStateMachine;
    @Mock
    private BrandProperties brandProperties;
    @Mock
    private EmailService emailService;
    @Mock
    private PaymentRecordRepository paymentRecordRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    // --- Stripe initiation ---
    @Test
    void shouldCreateStripeSessionAndAttachToAppointment() {
        // given
        var appointment = createAppointment(AppointmentState.RESERVED);
        var command = PaymentCommand.builder()
                .appointment(appointment)
                .paymentMethod(PaymentMethodChoice.STRIPE)
                .build();

        when(brandProperties.getCurrency()).thenReturn("EUR");
        when(paymentProvider.createSession(any())).thenReturn(
                ProviderSession.builder()
                        .sessionId("cs_test_123")
                        .redirectUrl("https://checkout.stripe.com/cs_test_123")
                        .build()
        );

        // when
        var result = paymentService.initPayment(command);

        // then
        assertThat(result.getStripeRedirectUrl()).isEqualTo("https://checkout.stripe.com/cs_test_123");
        assertThat(appointment.getStripeSessionId()).isEqualTo("cs_test_123");
        verify(appointmentRepository).save(appointment);

        var captor = ArgumentCaptor.forClass(ProviderRequest.class);
        verify(paymentProvider).createSession(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(captor.getValue().getCurrency()).isEqualTo("EUR");
    }

    // --- Bank transfer ---
    @Test
    void shouldTransitionToPendingPaymentForBankTransfer() {
        // given
        var appointment = createAppointment(AppointmentState.RESERVED);
        var command = PaymentCommand.builder()
                .appointment(appointment)
                .paymentMethod(PaymentMethodChoice.BANK_TRANSFER)
                .build();
        // when
        paymentService.initPayment(command);

        // then
        verify(appointmentStateMachine).transition(appointment, AppointmentState.PENDING_PAYMENT, "SYSTEM_BANK_TRANSFER");
        verify(appointmentRepository).save(appointment);
        verify(emailService).sendPendingPaymentEmail(appointment);
    }

    // --- Webhook confirmation: happy path ---
    @Test
    void shouldTransitionToPaidOnWebhookConfirmation() {
        // given
        var appointmentId = UUID.randomUUID();
        var sessionId = "cs_test_123";
        var appointment = createAppointment(AppointmentState.RESERVED);
        appointment.updateStripeSessionId(sessionId);
        setId(appointment, appointmentId);
        var slot = appointment.getTimeSlot();

        var providerResult = ProviderResult.builder()
                .appointmentId(appointmentId)
                .sessionId(sessionId)
                .outcome(ProviderResult.PaymentOutcome.SUCCESS)
                .amount(new BigDecimal("30.00"))
                .build();

        when(appointmentRepository.findByIdAndStripeSessionId(appointmentId, sessionId))
                .thenReturn(Optional.of(appointment));

        // when
        paymentService.handleStripeWebhookConfirmation(providerResult);

        // then
        verify(appointmentStateMachine).transition(appointment, AppointmentState.PAID, "STRIPE_WEBHOOK");
        verify(timeSlotStateMachine).transition(slot, TimeSlotState.BOOKED, "STRIPE_WEBHOOK");
        verify(appointmentRepository).save(appointment);
        verify(timeSlotRepository).save(slot);

        var captor = ArgumentCaptor.forClass(PaymentRecord.class);
        verify(paymentRecordRepository).save(captor.capture());
        var paymentRecord = captor.getValue();
        assertThat(paymentRecord.getAppointment()).isEqualTo(appointment);
        assertThat(paymentRecord.getAmount()).isEqualTo(appointment.getFinalPrice());
        assertThat(paymentRecord.getPaymentMethod()).isEqualTo(PaymentMethod.STRIPE);
        assertThat(paymentRecord.getPaymentDate()).isEqualTo(LocalDate.now());
        assertThat(paymentRecord.getStripePaymentId()).isEqualTo(sessionId);

        verify(emailService).sendConfirmationEmail(appointment);
    }

    // --- Webhook confirmation: idempotent (already PAID) ---
    @Test
    void shouldIgnoreWebhookWhenAlreadyPaid() {
        // given
        var appointmentId = UUID.randomUUID();
        var sessionId = "cs_test_123";
        var appointment = createAppointment(AppointmentState.PAID);
        setId(appointment, appointmentId);
        var slot = appointment.getTimeSlot();

        var providerResult = ProviderResult.builder()
                .appointmentId(appointmentId)
                .sessionId(sessionId)
                .outcome(ProviderResult.PaymentOutcome.SUCCESS)
                .amount(new BigDecimal("30.00"))
                .build();

        when(appointmentRepository.findByIdAndStripeSessionId(appointmentId, sessionId))
                .thenReturn(Optional.of(appointment));

        // when
        paymentService.handleStripeWebhookConfirmation(providerResult);

        // then
        verify(appointmentStateMachine, never()).transition(any(), any(), any());
        verify(timeSlotStateMachine, never()).transition(any(), any(), any());
        verify(appointmentRepository, never()).save(appointment);
        verify(timeSlotRepository, never()).save(slot);
    }

    // --- Webhook confirmation: appointment not found ---
    @Test
    void shouldThrowWhenAppointmentNotFoundForWebhook() {
        // given
        var providerResult = ProviderResult.builder()
                .appointmentId(UUID.randomUUID())
                .sessionId("cs_unknown")
                .outcome(ProviderResult.PaymentOutcome.SUCCESS)
                .amount(new BigDecimal("30.00"))
                .build();

        when(appointmentRepository.findByIdAndStripeSessionId(any(), any()))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> paymentService.handleStripeWebhookConfirmation(providerResult))
                .isInstanceOf(WebhookVerificationException.class);
    }

    // --- Helpers ---
    private Appointment createAppointment(AppointmentState state) {
        var slot = TimeSlot.create(LocalDate.of(2026, 6, 20), LocalTime.of(10, 0));
        slot.transitionTo(TimeSlotState.RESERVED);
        var category = ServiceCategory.create("Math Lesson", "Algebra", new BigDecimal("30.00"), "EUR");
        var student = Student.create("Ana", "ana@test.com", "+385 91 123 4567");

        return Appointment.create(
                slot, category, student, state,
                new BigDecimal("30.00"), new BigDecimal("30.00"),
                AppointmentOrigin.STOREFRONT, "Notes"
        );
    }

    private void setId(Object entity, UUID id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
