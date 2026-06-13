package com.studioengine.tutor.scheduling;

import com.studioengine.tutor.dataaccess.entities.TimeSlot;
import com.studioengine.tutor.dataaccess.entities.TimeSlotStateLog;
import com.studioengine.tutor.dataaccess.enums.TimeSlotState;
import com.studioengine.tutor.dataaccess.repositories.TimeSlotStateLogRepository;
import com.studioengine.tutor.errors.exceptions.InvalidStateTransitionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TimeSlotStateMachine {

    private static final Map<TimeSlotState, Set<TimeSlotState>> TRANSITIONS = Map.of(
            TimeSlotState.DRAFT, Set.of(TimeSlotState.AVAILABLE, TimeSlotState.PRE_BOOKED),
            TimeSlotState.AVAILABLE, Set.of(TimeSlotState.RESERVED, TimeSlotState.BOOKED, TimeSlotState.PRE_BOOKED, TimeSlotState.DRAFT),
            TimeSlotState.RESERVED, Set.of(TimeSlotState.BOOKED, TimeSlotState.AVAILABLE),
            TimeSlotState.BOOKED, Set.of(TimeSlotState.AVAILABLE),
            TimeSlotState.PRE_BOOKED, Set.of(TimeSlotState.AVAILABLE)
    );

    private final TimeSlotStateLogRepository slotStateLogRepository;

    public void transition(TimeSlot slot, TimeSlotState target, String triggeredBy) {
        var current = slot.getState();
        verifyValidTransition(current, target);

        var log = TimeSlotStateLog.create(slot, current, target, triggeredBy);
        slot.transitionTo(target);
        slotStateLogRepository.save(log);
    }

    private void verifyValidTransition(TimeSlotState current, TimeSlotState target) {
        var allowed = TRANSITIONS.getOrDefault(current, Set.of());

        if (!allowed.contains(target)) {
            throw new InvalidStateTransitionException("TimeSlot cannot transition from %s to %s".formatted(current, target));
        }
    }
}
