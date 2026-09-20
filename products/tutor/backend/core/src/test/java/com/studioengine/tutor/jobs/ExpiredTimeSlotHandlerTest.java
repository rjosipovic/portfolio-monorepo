package com.studioengine.tutor.jobs;

import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.TimeSlot;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.dataaccess.enums.TimeSlotState;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import com.studioengine.tutor.dataaccess.repositories.TimeSlotRepository;
import com.studioengine.tutor.scheduling.AppointmentStateMachine;
import com.studioengine.tutor.scheduling.TimeSlotStateMachine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpiredTimeSlotHandlerTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private TimeSlotRepository timeSlotRepository;
    @Mock
    private TimeSlotStateMachine timeSlotStateMachine;
    @Mock
    private AppointmentStateMachine appointmentStateMachine;

    @InjectMocks
    private ExpiredTimeSlotHandler expiredTimeSlotHandler;

    @Test
    void shouldReleaseTimeSlotAndCancelAppointment() {
        // given
        var timeSlotId = UUID.randomUUID();
        var timeSlot = mock(TimeSlot.class);
        when(timeSlot.getId()).thenReturn(timeSlotId);
        var lookupStates = Set.of(AppointmentState.RESERVED, AppointmentState.PENDING_PAYMENT);
        var reservedAppointment = mock(Appointment.class);
        when(reservedAppointment.getState()).thenReturn(AppointmentState.RESERVED);
        when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.of(timeSlot));
        when(appointmentRepository.findByTimeSlotIdAndStateIn(timeSlotId, lookupStates)).thenReturn(Optional.of(reservedAppointment));

        // when
        expiredTimeSlotHandler.handle(timeSlotId);

        // then
        verify(timeSlotRepository).findById(timeSlotId);
        verify(appointmentRepository).findByTimeSlotIdAndStateIn(timeSlotId, lookupStates);
        verify(timeSlotStateMachine).transition(timeSlot, TimeSlotState.AVAILABLE, "SYSTEM_TIMEOUT");
        verify(timeSlotRepository).save(timeSlot);
        verify(appointmentStateMachine).transition(reservedAppointment, AppointmentState.CANCELLED, "SYSTEM_TIMEOUT");
        verify(appointmentRepository).save(reservedAppointment);
    }

    @Test
    void shouldNotReleaseTimeSlotAndCancelAppointmentWhenTimeSlotNotFound() {
        // given
        var timeSlotId = UUID.randomUUID();
        when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.empty());

        // when
        assertThatThrownBy(() ->  expiredTimeSlotHandler.handle(timeSlotId)).isInstanceOf(IllegalStateException.class);

        // then
        verify(timeSlotRepository).findById(timeSlotId);
        verify(appointmentRepository, never()).findByTimeSlotIdAndStateIn(any(), any());
        verify(timeSlotStateMachine, never()).transition(any(), any(), any());
        verify(timeSlotRepository, never()).save(any());
    }

    @Test
    void shouldSkipReleaseTimeSlotWhenInPendingPayment() {
        // given
        var timeSlotId = UUID.randomUUID();
        var timeSlot = mock(TimeSlot.class);
        when(timeSlot.getId()).thenReturn(timeSlotId);
        var lookupStates = Set.of(AppointmentState.RESERVED, AppointmentState.PENDING_PAYMENT);
        var pendingPaymentAppointment = mock(Appointment.class);
        when(pendingPaymentAppointment.getState()).thenReturn(AppointmentState.PENDING_PAYMENT);
        when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.of(timeSlot));
        when(appointmentRepository.findByTimeSlotIdAndStateIn(timeSlotId, lookupStates)).thenReturn(Optional.of(pendingPaymentAppointment));

        // when
        expiredTimeSlotHandler.handle(timeSlotId);

        // then
        verify(timeSlotRepository).findById(timeSlotId);
        verify(appointmentRepository).findByTimeSlotIdAndStateIn(timeSlotId, lookupStates);
        verify(timeSlotStateMachine, never()).transition(any(), any(), any());
        verify(appointmentStateMachine, never()).transition(any(), any(), any());
    }

    @Test
    void shouldReleaseTimeSlotAndNotCancelAppointmentWhenAppointmentNotExists() {
        // given
        var timeSlotId = UUID.randomUUID();
        var timeSlot = mock(TimeSlot.class);
        when(timeSlot.getId()).thenReturn(timeSlotId);
        var lookupStates = Set.of(AppointmentState.RESERVED, AppointmentState.PENDING_PAYMENT);
        when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.of(timeSlot));
        when(appointmentRepository.findByTimeSlotIdAndStateIn(timeSlotId, lookupStates)).thenReturn(Optional.empty());

        // when
        expiredTimeSlotHandler.handle(timeSlotId);

        // then
        verify(timeSlotRepository).findById(timeSlotId);
        verify(appointmentRepository).findByTimeSlotIdAndStateIn(timeSlotId, lookupStates);
        verify(timeSlotStateMachine).transition(timeSlot, TimeSlotState.AVAILABLE, "SYSTEM_TIMEOUT");
        verify(timeSlotRepository).save(timeSlot);
        verify(appointmentStateMachine, never()).transition(any(), any(), any());
    }
}