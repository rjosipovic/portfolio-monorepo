package com.studioengine.tutor.scheduling;

import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.AppointmentStateLog;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.dataaccess.enums.TimeSlotState;
import com.studioengine.tutor.dataaccess.repositories.AppointmentStateLogRepository;
import com.studioengine.tutor.errors.exceptions.InvalidStateTransitionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

import static com.studioengine.tutor.dataaccess.enums.AppointmentState.CANCELLED;
import static com.studioengine.tutor.dataaccess.enums.AppointmentState.COMPLETED;
import static com.studioengine.tutor.dataaccess.enums.AppointmentState.CONFIRMED;
import static com.studioengine.tutor.dataaccess.enums.AppointmentState.NO_SHOW;
import static com.studioengine.tutor.dataaccess.enums.AppointmentState.PAID;
import static com.studioengine.tutor.dataaccess.enums.AppointmentState.PENDING_PAYMENT;
import static com.studioengine.tutor.dataaccess.enums.AppointmentState.PRE_BOOKED;
import static com.studioengine.tutor.dataaccess.enums.AppointmentState.RESERVED;

@Component
@RequiredArgsConstructor
public class AppointmentStateMachine {

    private static final Map<AppointmentState, Set<AppointmentState>> TRANSITIONS = Map.of(
            RESERVED, Set.of(PAID, PENDING_PAYMENT, CANCELLED),
            PENDING_PAYMENT, Set.of(CONFIRMED, CANCELLED),
            PAID, Set.of(COMPLETED, NO_SHOW, CANCELLED),
            CONFIRMED, Set.of(COMPLETED, NO_SHOW, CANCELLED),
            PRE_BOOKED, Set.of(COMPLETED, NO_SHOW, CANCELLED)
    );

    private static final Set<AppointmentState> TERMINAL_STATES = Set.of(COMPLETED, NO_SHOW, CANCELLED);

    private final AppointmentStateLogRepository stateLogRepository;
    private final TimeSlotStateMachine timeSlotStateMachine;

    public void transition(Appointment appointment, AppointmentState target, String triggeredBy) {
        var current = appointment.getState();
        verifyNotTerminal(current);
        verifyValidTransition(current, target);

        var log = AppointmentStateLog.create(appointment, current, target, triggeredBy);
        appointment.transitionTo(target);
        stateLogRepository.save(log);

        if (target == CANCELLED) {
            releaseSlot(appointment, triggeredBy);
        }
    }

    private void verifyNotTerminal(AppointmentState current) {
        if (TERMINAL_STATES.contains(current)) {
            throw new InvalidStateTransitionException(
                    "Appointment is in terminal state %s, no further transitions allowed".formatted(current)
            );
        }
    }

    private void verifyValidTransition(AppointmentState current, AppointmentState target) {
        var allowed = TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(target)) {
            throw new InvalidStateTransitionException(
                    "Appointment cannot transition from %s to %s".formatted(current, target)
            );
        }
    }

    private void releaseSlot(Appointment appointment, String triggeredBy) {
        var slot = appointment.getTimeSlot();
        timeSlotStateMachine.transition(slot, TimeSlotState.AVAILABLE, triggeredBy);
    }
}
