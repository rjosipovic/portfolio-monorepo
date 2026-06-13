package com.studioengine.tutor.scheduling;

import com.studioengine.tutor.dataaccess.entities.TimeSlot;
import com.studioengine.tutor.dataaccess.entities.TimeSlotStateLog;
import com.studioengine.tutor.dataaccess.enums.TimeSlotState;
import com.studioengine.tutor.dataaccess.repositories.TimeSlotStateLogRepository;
import com.studioengine.tutor.errors.exceptions.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.stream.Stream;

import static com.studioengine.tutor.dataaccess.enums.TimeSlotState.AVAILABLE;
import static com.studioengine.tutor.dataaccess.enums.TimeSlotState.BOOKED;
import static com.studioengine.tutor.dataaccess.enums.TimeSlotState.DRAFT;
import static com.studioengine.tutor.dataaccess.enums.TimeSlotState.PRE_BOOKED;
import static com.studioengine.tutor.dataaccess.enums.TimeSlotState.RESERVED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TimeSlotStateMachineTest {

    @Mock
    private TimeSlotStateLogRepository stateLogRepository;

    @InjectMocks
    private TimeSlotStateMachine stateMachine;

    // --- Valid transitions ---
    record Transition(TimeSlotState from, TimeSlotState to) {}

    static Stream<Transition> validTransitions() {
        return Stream.of(
                new Transition(DRAFT, AVAILABLE),
                new Transition(DRAFT, PRE_BOOKED),
                new Transition(AVAILABLE, RESERVED),
                new Transition(AVAILABLE, BOOKED),
                new Transition(AVAILABLE, DRAFT),
                new Transition(AVAILABLE, PRE_BOOKED),
                new Transition(RESERVED, BOOKED),
                new Transition(RESERVED, AVAILABLE),
                new Transition(BOOKED, AVAILABLE),
                new Transition(PRE_BOOKED, AVAILABLE)
        );
    }

    @ParameterizedTest
    @MethodSource("validTransitions")
    void shouldAllowValidTransition(Transition transition) {
        // given
        var slot = createSlotInState(transition.from());

        // when
        stateMachine.transition(slot, transition.to(), "TEST");

        // then
        assertThat(slot.getState()).isEqualTo(transition.to());
    }

    // --- Invalid transitions ---
    static Stream<Transition> invalidTransitions() {
        return Stream.of(
                new Transition(DRAFT, RESERVED),
                new Transition(DRAFT, BOOKED),
                new Transition(RESERVED, DRAFT),
                new Transition(RESERVED, PRE_BOOKED),
                new Transition(BOOKED, DRAFT),
                new Transition(BOOKED, RESERVED),
                new Transition(BOOKED, PRE_BOOKED),
                new Transition(PRE_BOOKED, DRAFT),
                new Transition(PRE_BOOKED, RESERVED),
                new Transition(PRE_BOOKED, BOOKED)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidTransitions")
    void shouldRejectInvalidTransition(Transition transition) {
        // given
        var slot = createSlotInState(transition.from());

        // when / then
        assertThatThrownBy(() -> stateMachine.transition(slot, transition.to(), "TEST"))
                .isInstanceOf(InvalidStateTransitionException.class);
        assertThat(slot.getState()).isEqualTo(transition.from());
    }

    // --- Audit log ---
    @Test
    void shouldCreateAuditLogOnTransition() {
        // given
        var slot = createSlotInState(DRAFT);
        var captor = ArgumentCaptor.forClass(TimeSlotStateLog.class);

        // when
        stateMachine.transition(slot, AVAILABLE, "TUTOR");

        // then
        verify(stateLogRepository).save(captor.capture());
        var log = captor.getValue();
        assertThat(log.getFromState()).isEqualTo(DRAFT);
        assertThat(log.getToState()).isEqualTo(AVAILABLE);
        assertThat(log.getTriggeredBy()).isEqualTo("TUTOR");
    }

    // --- Helper ---
    private TimeSlot createSlotInState(TimeSlotState state) {
        var slot = TimeSlot.create(
                LocalDate.of(2026, 6, 15),
                LocalTime.of(10, 0)
        );
        if (state != DRAFT) {
            slot.transitionTo(state);
        }
        return slot;
    }
}
