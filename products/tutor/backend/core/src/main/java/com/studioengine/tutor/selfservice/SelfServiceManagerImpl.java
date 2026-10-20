package com.studioengine.tutor.selfservice;

import com.studioengine.tutor.config.SchedulingProperties;
import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.CancellationToken;
import com.studioengine.tutor.dataaccess.enums.AppointmentOrigin;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.dataaccess.enums.TimeSlotState;
import com.studioengine.tutor.dataaccess.enums.TokenType;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import com.studioengine.tutor.dataaccess.repositories.CancellationTokenRepository;
import com.studioengine.tutor.dataaccess.repositories.TimeSlotRepository;
import com.studioengine.tutor.email.EmailService;
import com.studioengine.tutor.errors.exceptions.DeadlinePassedException;
import com.studioengine.tutor.errors.exceptions.PreBookedSelfServiceException;
import com.studioengine.tutor.errors.exceptions.ResourceNotFoundException;
import com.studioengine.tutor.errors.exceptions.TokenExpiredException;
import com.studioengine.tutor.scheduling.AppointmentStateMachine;
import com.studioengine.tutor.scheduling.TimeSlotStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SelfServiceManagerImpl implements SelfServiceManager {

    private static final Set<AppointmentState> RESCHEDULABLE_STATES = Set.of(
            AppointmentState.PENDING_PAYMENT,
            AppointmentState.PAID,
            AppointmentState.CONFIRMED
    );

    private static final String TRIGGERED_BY = "STUDENT_SELF_SERVICE";

    private final CancellationTokenRepository cancellationTokenRepository;
    private final SchedulingProperties schedulingProperties;
    private final AppointmentStateMachine appointmentStateMachine;
    private final AppointmentRepository appointmentRepository;
    private final TimeSlotStateMachine timeSlotStateMachine;
    private final TimeSlotRepository timeSlotRepository;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public AppointmentDetails validateToken(String token) {
        var cancellationToken = findAndValidateToken(token);
        var appointment = cancellationToken.getAppointment();
        var slot = appointment.getTimeSlot();

        var deadlinePassed = isDeadlinePassed(slot.getSlotDate().atTime(slot.getStartTime()));

        return AppointmentDetails.builder()
                .appointmentId(appointment.getId())
                .studentName(appointment.getStudent().getName())
                .serviceCategoryName(appointment.getServiceCategory().getName())
                .date(slot.getSlotDate())
                .startTime(slot.getStartTime())
                .deadlineMissed(deadlinePassed)
                .build();
    }

    @Override
    @Transactional
    public AppointmentCancellation confirmCancellation(String token) {
        var cancellationToken = findAndValidateToken(token);
        var appointment = cancellationToken.getAppointment();
        cancelAndRelease(appointment);
        markTokenAsUsed(cancellationToken);

        emailService.sendCancellationNotification(appointment, "Student self-cancellation");

        log.info("Student self-canceled appointment {}", appointment.getId());

        return AppointmentCancellation.builder()
                .appointmentId(appointment.getId())
                .message("Appointment cancelled")
                .build();
    }

    @Override
    @Transactional
    public RescheduleInitiation confirmReschedule(String token) {
        var cancellationToken = findAndValidateToken(token);
        var appointment = cancellationToken.getAppointment();
        var originalState = appointment.getState();
        cancelAndRelease(appointment);
        markTokenAsUsed(cancellationToken);

        var rescheduleToken = CancellationToken.createReschedule(
                appointment,
                OffsetDateTime.now(clock).plus(schedulingProperties.getRescheduleTokenExpiry()),
                originalState
        );
        cancellationTokenRepository.save(rescheduleToken);

        return RescheduleInitiation.builder()
                .originalAppointmentId(appointment.getId())
                .rescheduleToken(rescheduleToken.getToken())
                .build();
    }

    @Override
    @Transactional
    public void completeReschedule(UUID slotId, String rescheduleToken) {
        // 1. Validate reschedule token
        var token = cancellationTokenRepository.findByToken(rescheduleToken).orElseThrow(() -> new TokenExpiredException("Reschedule token not found"));
        if (token.isUsed()) throw new TokenExpiredException("Token already used");
        if (token.isExpired()) throw new TokenExpiredException("Token expired");
        if (token.getTokenType() != TokenType.RESCHEDULE_BOOKING) throw new TokenExpiredException("Invalid token type");

        // 2. Get student from original appointment
        var originalAppointment = token.getAppointment();
        var student = originalAppointment.getStudent();
        var category = originalAppointment.getServiceCategory();

        // 3. Reserve and book slot atomically
        var slot = timeSlotRepository.findById(slotId).orElseThrow(() -> new ResourceNotFoundException("TimeSlot not found"));
        if (slot.getState() != TimeSlotState.AVAILABLE) throw new IllegalStateException("Slot not available");
        var appointmentState = token.getOriginalState();

        if (!RESCHEDULABLE_STATES.contains(appointmentState)) {
            throw new IllegalStateException("Appointment in state %s cannot be rescheduled".formatted(appointmentState));
        }

        var slotState = (appointmentState == AppointmentState.PENDING_PAYMENT) ? TimeSlotState.RESERVED : TimeSlotState.BOOKED;
        timeSlotStateMachine.transition(slot, slotState, "RESCHEDULE");
        timeSlotRepository.save(slot);

        // 4. Create appointment directly in carried state
        var appointment = Appointment.create(
                slot, category, student,
                appointmentState,
                originalAppointment.getOriginalPrice(),
                originalAppointment.getFinalPrice(),
                AppointmentOrigin.STOREFRONT,
                null);
        appointmentRepository.save(appointment);

        // 5. Mark token used
        markTokenAsUsed(token);

        // 6. Send confirmation + generate manage link
        var manageLink = tokenService.generateManageLink(appointment);
        if (appointmentState == AppointmentState.PENDING_PAYMENT) {
            emailService.sendPendingPaymentEmail(appointment, manageLink);
        } else {
            emailService.sendConfirmationEmail(appointment, manageLink);
        }

        // 7. notify tutor
        emailService.sendRescheduleNotification(originalAppointment, appointment);
    }

    private void cancelAndRelease(Appointment appointment) {
        var appointmentState = appointment.getState();
        var slot = appointment.getTimeSlot();

        if (!RESCHEDULABLE_STATES.contains(appointmentState)) {
            throw new IllegalStateException("Appointment in state %s cannot be canceled".formatted(appointmentState));
        }

        enforceDeadline(slot.getSlotDate().atTime(slot.getStartTime()));

        appointmentStateMachine.transition(appointment, AppointmentState.CANCELLED, TRIGGERED_BY);
        timeSlotStateMachine.transition(slot, TimeSlotState.AVAILABLE, TRIGGERED_BY);

        appointmentRepository.save(appointment);
        timeSlotRepository.save(slot);
    }

    private CancellationToken findAndValidateToken(String token) {
        var cancellationToken =  cancellationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token %s not found".formatted(token)));

        if (cancellationToken.isUsed()) {
            throw new TokenExpiredException("Token already used");
        }

        if (cancellationToken.isExpired()) {
            throw new TokenExpiredException("Token expired");
        }

        var appointment = cancellationToken.getAppointment();

        if (appointment.getState() == AppointmentState.PRE_BOOKED) {
            throw new PreBookedSelfServiceException("Self-service not available for direct bookings");
        }
        return cancellationToken;
    }

    private void enforceDeadline(LocalDateTime appointmentStart) {
        if (isDeadlinePassed(appointmentStart)) {
            throw new DeadlinePassedException("Cancellation deadline has passed.");
        }
    }

    private boolean isDeadlinePassed(LocalDateTime appointmentStart) {
        var cancellationDeadline = schedulingProperties.getCancellationDeadline();
        var zone = clock.getZone();
        var deadline = appointmentStart.atZone(zone).minus(cancellationDeadline);
        var now = ZonedDateTime.now(clock);
        return now.isAfter(deadline);
    }

    private void markTokenAsUsed(CancellationToken token) {
        token.markUsed();
        cancellationTokenRepository.save(token);
    }
}
