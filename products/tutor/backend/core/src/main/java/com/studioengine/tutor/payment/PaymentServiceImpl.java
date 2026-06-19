package com.studioengine.tutor.payment;

import com.studioengine.tutor.config.BrandProperties;
import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.dataaccess.enums.TimeSlotState;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import com.studioengine.tutor.dataaccess.repositories.TimeSlotRepository;
import com.studioengine.tutor.errors.exceptions.WebhookVerificationException;
import com.studioengine.tutor.payment.provider.ProviderRequest;
import com.studioengine.tutor.payment.provider.ProviderResult;
import com.studioengine.tutor.payment.provider.ProviderSession;
import com.studioengine.tutor.scheduling.AppointmentStateMachine;
import com.studioengine.tutor.scheduling.TimeSlotStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentProvider paymentProvider;
    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final AppointmentStateMachine appointmentStateMachine;
    private final TimeSlotStateMachine timeSlotStateMachine;
    private final BrandProperties brandProperties;


    @Override
    public PaymentInitiation initPayment(PaymentCommand ctx) {

        var paymentChoice = ctx.getPaymentMethod();
        var appointment = ctx.getAppointment();

        return switch (paymentChoice) {
            case STRIPE -> initiateStripePayment(appointment);
            case BANK_TRANSFER -> initiateBankTransfer(appointment);
        };
    }

    private PaymentInitiation initiateStripePayment(Appointment appointment) {
        var request = ProviderRequest.builder()
                .appointmentId(appointment.getId())
                .amount(appointment.getFinalPrice())
                .currency(brandProperties.getCurrency())
                .description("Lesson: " + appointment.getServiceCategory().getName())
                .build();

        var session = paymentProvider.createSession(request);
        attachSessionId(appointment, session);

        return PaymentInitiation.builder()
                .appointmentId(appointment.getId())
                .resultingState(appointment.getState())
                .stripeRedirectUrl(session.getRedirectUrl())
                .build();
    }

    private void attachSessionId(Appointment appointment, ProviderSession session) {
        appointment.updateStripeSessionId(session.getSessionId());
        appointmentRepository.save(appointment);
    }

    private PaymentInitiation initiateBankTransfer(Appointment appointment) {
        appointmentStateMachine.transition(appointment, AppointmentState.PENDING_PAYMENT, "SYSTEM_BANK_TRANSFER");
        appointmentRepository.save(appointment);

        // TODO: trigger PDF generation + email when we build EmailService

        return PaymentInitiation.builder()
                .appointmentId(appointment.getId())
                .resultingState(appointment.getState())
                .build();
    }

    @Transactional
    public void handleStripeWebhookConfirmation(ProviderResult result) {
        findAppointment(result).ifPresent(appointment -> {
            appointmentStateMachine.transition(appointment, AppointmentState.PAID, "STRIPE_WEBHOOK");
            timeSlotStateMachine.transition(appointment.getTimeSlot(), TimeSlotState.BOOKED, "STRIPE_WEBHOOK");
            appointmentRepository.save(appointment);
            timeSlotRepository.save(appointment.getTimeSlot());
            // TODO: create PaymentRecord + send confirmation email when those services exist
        });
    }

    private Optional<Appointment> findAppointment(ProviderResult result) {
        var appointment = appointmentRepository.findByIdAndStripeSessionId(result.getAppointmentId(), result.getSessionId())
                .orElseThrow(() -> new WebhookVerificationException("No matching appointment for webhook payload"));

        return Optional.of(appointment)
                .filter(a -> a.getState() != AppointmentState.PAID)
                .or(() -> {
                    log.info("Appointment {} already PAID — idempotent webhook ignored", appointment.getId());
                    return Optional.empty();
                });
    }
}
