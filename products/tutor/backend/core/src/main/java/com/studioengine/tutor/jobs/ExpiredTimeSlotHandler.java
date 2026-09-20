package com.studioengine.tutor.jobs;

import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.TimeSlot;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.dataaccess.enums.TimeSlotState;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import com.studioengine.tutor.dataaccess.repositories.TimeSlotRepository;
import com.studioengine.tutor.scheduling.AppointmentStateMachine;
import com.studioengine.tutor.scheduling.TimeSlotStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpiredTimeSlotHandler {

    private static final String TRIGGERED_BY = "SYSTEM_TIMEOUT";

    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final TimeSlotStateMachine timeSlotStateMachine;
    private final AppointmentStateMachine appointmentStateMachine;

    @Transactional
    public void handle(UUID timeSlotId) {
        var timeSlot = findTimeSlot(timeSlotId);
        appointmentRepository.findByTimeSlotIdAndStateIn(timeSlot.getId(), Set.of(AppointmentState.RESERVED, AppointmentState.PENDING_PAYMENT))
                .ifPresentOrElse(
                        appointment -> {
                            if (appointment.getState() == AppointmentState.RESERVED) {
                                makeTimeSlotAvailable(timeSlot);
                                makeAppointmentCanceled(appointment);
                                log.info("Released slot {} and canceled appointment {}", timeSlot.getId(), appointment.getId());
                            } else if (appointment.getState() == AppointmentState.PENDING_PAYMENT) {
                                log.debug("Skipping slot {} - appointment in [{}]", timeSlot.getId(), AppointmentState.PENDING_PAYMENT);
                            }
                        },
                        () -> makeTimeSlotAvailable(timeSlot));
    }

    private TimeSlot findTimeSlot(UUID timeSlotId) {
        return timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new IllegalStateException("TimeSlot not found: " + timeSlotId));
    }

    private void makeTimeSlotAvailable(TimeSlot timeSlot) {
        timeSlotStateMachine.transition(timeSlot, TimeSlotState.AVAILABLE, TRIGGERED_BY);
        timeSlotRepository.save(timeSlot);
    }

    private void makeAppointmentCanceled(Appointment appointment) {
        appointmentStateMachine.transition(appointment, AppointmentState.CANCELLED, TRIGGERED_BY);
        appointmentRepository.save(appointment);
    }
}
