package com.studioengine.tutor.jobs;

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

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpiredTimeSlotHandler {

    private static final String TRIGGERED_BY = "SYSTEM_TIMEOUT";
    private static final Set<AppointmentState> SKIP_STATES = Set.of(AppointmentState.PENDING_PAYMENT);

    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final TimeSlotStateMachine timeSlotStateMachine;
    private final AppointmentStateMachine appointmentStateMachine;

    @Transactional
    public void handle(TimeSlot timeSlot) {

        var shouldSkip = appointmentRepository.findByTimeSlotIdAndStateIn(timeSlot.getId(), SKIP_STATES).isPresent();

        if (shouldSkip) {
            log.debug("Skipping slot {} - appointment in [{}]", timeSlot.getId(), SKIP_STATES);
            return;
        }

        timeSlotStateMachine.transition(timeSlot, TimeSlotState.AVAILABLE, TRIGGERED_BY);
        timeSlotRepository.save(timeSlot);

        // Cancel RESERVED appointment if exists
        appointmentRepository.findByTimeSlotIdAndStateIn(timeSlot.getId(), Set.of(AppointmentState.RESERVED))
                .ifPresent(a -> {
                    appointmentStateMachine.transition(a, AppointmentState.CANCELLED, TRIGGERED_BY);
                    appointmentRepository.save(a);
                    log.info("Released slot {} and canceled appointment {}", timeSlot.getId(), a.getId());
                });
    }
}
