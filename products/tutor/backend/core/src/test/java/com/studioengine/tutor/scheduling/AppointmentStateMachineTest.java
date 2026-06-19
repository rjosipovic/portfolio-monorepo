package com.studioengine.tutor.scheduling;

import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.AppointmentStateLog;
import com.studioengine.tutor.dataaccess.entities.ServiceCategory;
import com.studioengine.tutor.dataaccess.entities.Student;
import com.studioengine.tutor.dataaccess.entities.TimeSlot;
import com.studioengine.tutor.dataaccess.enums.AppointmentOrigin;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.dataaccess.enums.TimeSlotState;
import com.studioengine.tutor.dataaccess.repositories.AppointmentStateLogRepository;
import com.studioengine.tutor.errors.exceptions.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.stream.Stream;

import static com.studioengine.tutor.dataaccess.enums.AppointmentState.CANCELLED;
import static com.studioengine.tutor.dataaccess.enums.AppointmentState.COMPLETED;
import static com.studioengine.tutor.dataaccess.enums.AppointmentState.CONFIRMED;
import static com.studioengine.tutor.dataaccess.enums.AppointmentState.NO_SHOW;
import static com.studioengine.tutor.dataaccess.enums.AppointmentState.PAID;
import static com.studioengine.tutor.dataaccess.enums.AppointmentState.PENDING_PAYMENT;
import static com.studioengine.tutor.dataaccess.enums.AppointmentState.PRE_BOOKED;
import static com.studioengine.tutor.dataaccess.enums.AppointmentState.RESERVED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AppointmentStateMachineTest {

    @Mock
    private AppointmentStateLogRepository stateLogRepository;

    @Mock
    private TimeSlotStateMachine timeSlotStateMachine;

    @InjectMocks
    private AppointmentStateMachine stateMachine;

    // --- Valid transitions ---
    record Transition(AppointmentState from, AppointmentState to) {}

    static Stream<Transition> validTransitions() {
        return Stream.of(
                new Transition(RESERVED, PAID),
                new Transition(RESERVED, PENDING_PAYMENT),
                new Transition(RESERVED, CANCELLED),
                new Transition(PENDING_PAYMENT, CONFIRMED),
                new Transition(PENDING_PAYMENT, CANCELLED),
                new Transition(PAID, COMPLETED),
                new Transition(PAID, NO_SHOW),
                new Transition(PAID, CANCELLED),
                new Transition(CONFIRMED, COMPLETED),
                new Transition(CONFIRMED, NO_SHOW),
                new Transition(CONFIRMED, CANCELLED),
                new Transition(PRE_BOOKED, COMPLETED),
                new Transition(PRE_BOOKED, NO_SHOW),
                new Transition(PRE_BOOKED, CANCELLED)
        );
    }

    @ParameterizedTest
    @MethodSource("validTransitions")
    void shouldAllowValidTransition(Transition transition) {
        // given
        var appointment = createAppointmentInState(transition.from());

        // when
        stateMachine.transition(appointment, transition.to(), "TEST");

        // then
        assertThat(appointment.getState()).isEqualTo(transition.to());
    }

    // --- Invalid transitions ---
    static Stream<Transition> invalidTransitions() {
        return Stream.of(
                new Transition(RESERVED, COMPLETED),
                new Transition(RESERVED, NO_SHOW),
                new Transition(RESERVED, CONFIRMED),
                new Transition(RESERVED, PRE_BOOKED),
                new Transition(PENDING_PAYMENT, PAID),
                new Transition(PENDING_PAYMENT, COMPLETED),
                new Transition(PENDING_PAYMENT, NO_SHOW),
                new Transition(PAID, PENDING_PAYMENT),
                new Transition(PAID, CONFIRMED),
                new Transition(CONFIRMED, PAID),
                new Transition(PRE_BOOKED, PAID),
                new Transition(PRE_BOOKED, PENDING_PAYMENT)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidTransitions")
    void shouldRejectInvalidTransition(Transition transition) {
        // given
        var appointment = createAppointmentInState(transition.from());

        // when / then
        assertThatThrownBy(() -> stateMachine.transition(appointment, transition.to(), "TEST"))
                .isInstanceOf(InvalidStateTransitionException.class);
        assertThat(appointment.getState()).isEqualTo(transition.from());
    }

    // --- Terminal states ---
    static Stream<AppointmentState> terminalStates() {
        return Stream.of(COMPLETED, NO_SHOW, CANCELLED);
    }

    @ParameterizedTest
    @MethodSource("terminalStates")
    void shouldRejectTransitionFromTerminalState(AppointmentState terminalState) {
        // given
        var appointment = createAppointmentInState(terminalState);

        // when / then
        assertThatThrownBy(() -> stateMachine.transition(appointment, PAID, "TEST"))
                .isInstanceOf(InvalidStateTransitionException.class);
        assertThat(appointment.getState()).isEqualTo(terminalState);
    }

    // --- Slot release on cancellation ---
    @Test
    void shouldReleaseSlotOnCancellation() {
        // given
        var appointment = createAppointmentInState(PAID);
        var slot = appointment.getTimeSlot();

        // when
        stateMachine.transition(appointment, CANCELLED, "TUTOR");

        // then
        verify(timeSlotStateMachine).transition(slot, TimeSlotState.AVAILABLE, "TUTOR");
    }

    @Test
    void shouldNotReleaseSlotOnNonCancellationTransition() {
        // given
        var appointment = createAppointmentInState(PAID);

        // when
        stateMachine.transition(appointment, COMPLETED, "TUTOR");

        // then
        verify(timeSlotStateMachine, never()).transition(any(), any(), any());
    }

    // --- Audit log ---
    @Test
    void shouldCreateAuditLogOnTransition() {
        // given
        var appointment = createAppointmentInState(RESERVED);
        var captor = ArgumentCaptor.forClass(AppointmentStateLog.class);

        // when
        stateMachine.transition(appointment, PAID, "STRIPE_WEBHOOK");

        // then
        verify(stateLogRepository).save(captor.capture());
        var log = captor.getValue();
        assertThat(log.getFromState()).isEqualTo(RESERVED);
        assertThat(log.getToState()).isEqualTo(PAID);
        assertThat(log.getTriggeredBy()).isEqualTo("STRIPE_WEBHOOK");
    }

    // --- Helper ---
    private Appointment createAppointmentInState(AppointmentState state) {
        var slot = TimeSlot.create(LocalDate.of(2026, 6, 20), LocalTime.of(10, 0));
        slot.transitionTo(TimeSlotState.RESERVED);
        var category = ServiceCategory.create("Math", "Algebra", new BigDecimal("30.00"), "EUR");
        var student = Student.create("Ana", "ana@test.com", "+385 91 123 4567");

        return Appointment.create(
                slot, category, student, state,
                new BigDecimal("30.00"), new BigDecimal("30.00"),
                AppointmentOrigin.STOREFRONT, null
        );
    }
}
