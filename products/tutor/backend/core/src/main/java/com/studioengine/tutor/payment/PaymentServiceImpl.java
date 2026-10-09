package com.studioengine.tutor.payment;

import com.studioengine.tutor.config.BrandProperties;
import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.PaymentRecord;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.dataaccess.enums.PaymentMethod;
import com.studioengine.tutor.dataaccess.enums.TimeSlotState;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import com.studioengine.tutor.dataaccess.repositories.PaymentRecordRepository;
import com.studioengine.tutor.dataaccess.repositories.TimeSlotRepository;
import com.studioengine.tutor.email.EmailService;
import com.studioengine.tutor.payment.provider.ProviderRequest;
import com.studioengine.tutor.payment.provider.ProviderResult;
import com.studioengine.tutor.payment.provider.ProviderSession;
import com.studioengine.tutor.scheduling.AppointmentStateMachine;
import com.studioengine.tutor.scheduling.TimeSlotStateMachine;
import com.studioengine.tutor.selfservice.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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
    private final EmailService emailService;
    private final PaymentRecordRepository paymentRecordRepository;
    private final TokenService tokenService;


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

        var manageLink = tokenService.generateManageLink(appointment);
        emailService.sendPendingPaymentEmail(appointment, manageLink);

        return PaymentInitiation.builder()
                .appointmentId(appointment.getId())
                .resultingState(appointment.getState())
                .build();
    }

    @Transactional
    public void handleStripeWebhookConfirmation(ProviderResult result) {
        var appointmentId = result.getAppointmentId();
        var sessionId = result.getSessionId();
        var appointmentOptional = appointmentRepository.findByIdAndStripeSessionId(appointmentId, sessionId);

        if (appointmentOptional.isEmpty()) {
            handleOrphanedAppointment(result, "no matching appointment");
            return;
        }
        var appointment = appointmentOptional.get();
        var state = appointment.getState();

        if (state == AppointmentState.PAID) {
            log.info("Appointment {} already PAID — idempotent webhook ignored", appointmentId);
            return;
        }
        if (state != AppointmentState.RESERVED) {
            // any unexpected state: paid into a non-reservable appointment
            handleOrphanedAppointment(result, "appointment in unexpected state %s".formatted(state));
            return;
        }

        // happy path: RESERVED -> PAID transition
        transitionAndSaveStates(appointment);
        var paymentRecord = PaymentRecord.create(
                appointment,
                appointment.getFinalPrice(),
                PaymentMethod.STRIPE,
                LocalDate.now(),
                appointment.getStripeSessionId()
        );
        paymentRecordRepository.save(paymentRecord);
        var manageLink = tokenService.generateManageLink(appointment);
        emailService.sendConfirmationEmail(appointment, manageLink);
    }

    private void handleOrphanedAppointment(ProviderResult result, String why) {
        var appointmentId = result.getAppointmentId();
        var sessionId = result.getSessionId();
        var amount = result.getAmount();
        log.error("Stripe payment received but cannot be applied ({}): appointmentId={} sessionId={} amount={}", why, appointmentId, sessionId, amount);
        emailService.sendOrphanPaymentAlert(appointmentId, sessionId, amount);
    }

    private void transitionAndSaveStates(Appointment appointment) {
        appointmentStateMachine.transition(appointment, AppointmentState.PAID, "STRIPE_WEBHOOK");
        timeSlotStateMachine.transition(appointment.getTimeSlot(), TimeSlotState.BOOKED, "STRIPE_WEBHOOK");
        appointmentRepository.save(appointment);
        timeSlotRepository.save(appointment.getTimeSlot());
    }
}
