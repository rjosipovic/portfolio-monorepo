package com.studioengine.tutor.appointment;

import com.studioengine.tutor.config.BrandProperties;
import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import com.studioengine.tutor.email.EmailService;
import com.studioengine.tutor.errors.exceptions.MissingCancellationReasonException;
import com.studioengine.tutor.errors.exceptions.PrematureClosureException;
import com.studioengine.tutor.errors.exceptions.ResourceNotFoundException;
import com.studioengine.tutor.scheduling.AppointmentStateMachine;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentStateMachine appointmentStateMachine;
    private final BrandProperties brandProperties;
    private final EmailService emailService;

    @Override
    @Transactional
    public ClosedAppointment close(CloseAppointmentCommand command) {
        var appointment = findAppointment(command.getAppointmentId());
        verifyEndTimePassed(appointment);
        var targetState = determineTargetState(command.getOutcome());
        appointmentStateMachine.transition(appointment, targetState, "TUTOR");
        appointmentRepository.save(appointment);

        if (command.isSendFollowup() && targetState == AppointmentState.COMPLETED) {
            emailService.sendFollowUp(appointment);
        }

        return ClosedAppointment.builder()
                .appointmentId(appointment.getId())
                .state(appointment.getState())
                .build();
    }

    @Override
    @Transactional
    public CanceledAppointment cancel(CancelAppointmentCommand command) {
        var appointment = findAppointment(command.getAppointmentId());
        var reason = command.getReason();
        verifyReasonProvided(reason);
        appointmentStateMachine.transition(appointment, AppointmentState.CANCELLED, "TUTOR");
        appointmentRepository.save(appointment);

        emailService.sendCancellationNotification(appointment, reason);

        return CanceledAppointment.builder()
                .appointmentId(appointment.getId())
                .state(appointment.getState())
                .build();
    }

    private Appointment findAppointment(UUID id) {
        return appointmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));
    }

    private void verifyEndTimePassed(Appointment appointment) {
        var timezone = ZoneId.of(brandProperties.getTimezone());
        var now = LocalDate.now(timezone).atTime(LocalTime.now(timezone));
        var slotEnd = appointment.getTimeSlot().getSlotDate().atTime(appointment.getTimeSlot().getEndTime());
        if (now.isBefore(slotEnd)) {
            throw new PrematureClosureException("Cannot close appointment %s - end time %s has not passed".formatted(appointment.getId(), slotEnd));
        }
    }

    private void verifyReasonProvided(String reason) {
        if (Objects.isNull(reason) || reason.isBlank()) {
            throw new MissingCancellationReasonException("Cancellation reason is required");
        }
    }

    private AppointmentState determineTargetState(CloseAppointmentCommand.CloseOutcome closeOutcome) {
        return switch (closeOutcome) {
            case COMPLETED -> AppointmentState.COMPLETED;
            case NO_SHOW -> AppointmentState.NO_SHOW;
        };
    }
}
