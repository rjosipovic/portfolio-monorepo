package com.studioengine.tutor.selfservice;

import com.studioengine.tutor.config.InstanceProperties;
import com.studioengine.tutor.config.SchedulingProperties;
import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.CancellationToken;
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

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SelfServiceManagerImpl implements SelfServiceManager {

    private static final String TRIGGERED_BY = "STUDENT_SELF_SERVICE";

    private final CancellationTokenRepository cancellationTokenRepository;
    private final SchedulingProperties schedulingProperties;
    private final AppointmentStateMachine appointmentStateMachine;
    private final AppointmentRepository appointmentRepository;
    private final TimeSlotStateMachine timeSlotStateMachine;
    private final TimeSlotRepository timeSlotRepository;
    private final EmailService emailService;
    private final InstanceProperties instanceProperties;

    @Override
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
        var appointment = cancelAndRelease(token);

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
        var appointment = cancelAndRelease(token);

        var rescheduleToken = CancellationToken.create(
                appointment,
                TokenType.RESCHEDULE_BOOKING,
                OffsetDateTime.now().plusHours(24)
        );
        cancellationTokenRepository.save(rescheduleToken);

        var redirectUrl = "%s%s%s".formatted(
                instanceProperties.getBaseUrl(),
                "/api/v1/storefront/availability?rescheduleToken=",
                rescheduleToken.getToken());

        return RescheduleInitiation.builder()
                .originalAppointmentId(appointment.getId())
                .rescheduleToken(rescheduleToken.getToken())
                .redirectUrl(redirectUrl)
                .build();
    }

    private Appointment cancelAndRelease(String token) {
        var cancellationToken = findAndValidateToken(token);
        var appointment = cancellationToken.getAppointment();
        var slot = appointment.getTimeSlot();

        enforceDeadline(slot.getSlotDate().atTime(slot.getStartTime()));

        appointmentStateMachine.transition(appointment, AppointmentState.CANCELLED, TRIGGERED_BY);
        timeSlotStateMachine.transition(slot, TimeSlotState.AVAILABLE, TRIGGERED_BY);

        appointmentRepository.save(appointment);
        timeSlotRepository.save(slot);

        cancellationToken.markUsed();
        cancellationTokenRepository.save(cancellationToken);
        return appointment;
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
        var deadline = appointmentStart.minus(cancellationDeadline);
        var now = LocalDateTime.now();
        return now.isAfter(deadline);
    }
}
