package com.studioengine.tutor.appointment;

import com.studioengine.tutor.config.BrandProperties;
import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.TimeSlot;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import com.studioengine.tutor.email.EmailService;
import com.studioengine.tutor.errors.exceptions.MissingCancellationReasonException;
import com.studioengine.tutor.errors.exceptions.PrematureClosureException;
import com.studioengine.tutor.errors.exceptions.ResourceNotFoundException;
import com.studioengine.tutor.scheduling.AppointmentStateMachine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private AppointmentStateMachine appointmentStateMachine;
    @Mock
    private BrandProperties brandProperties;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    @ParameterizedTest
    @MethodSource("closeScenarios")
    void shouldClose(CloseScenario closeScenario) {
        // given
        var appointmentId = UUID.randomUUID();
        var appointment = mock(Appointment.class);
        var timeSlot = mock(TimeSlot.class);
        var date = LocalDate.now().minusDays(1);
        var startTime = LocalTime.of(10, 0);
        var command = CloseAppointmentCommand.builder()
                .appointmentId(appointmentId)
                .outcome(closeScenario.outcome)
                .sendFollowup(closeScenario.sendFollowup)
                .build();
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointment.getId()).thenReturn(appointmentId);
        when(appointment.getTimeSlot()).thenReturn(timeSlot);
        when(timeSlot.getSlotDate()).thenReturn(date);
        when(timeSlot.getEndTime()).thenReturn(startTime.plusHours(1));
        when(brandProperties.getTimezone()).thenReturn(String.valueOf(TimeZone.getDefault().toZoneId()));
        var expectedState = switch (closeScenario.outcome) {
            case COMPLETED -> AppointmentState.COMPLETED;
            case NO_SHOW -> AppointmentState.NO_SHOW;
        };
        when(appointment.getState()).thenReturn(expectedState);

        // when
        var result = appointmentService.close(command);

        // then
        if (closeScenario.expectEmail) {
            verify(emailService).sendFollowUp(appointment);
        } else {
            verify(emailService, never()).sendFollowUp(appointment);
        }
        verify(appointmentStateMachine).transition(appointment, expectedState, "TUTOR");
        verify(brandProperties).getTimezone();
        verify(appointmentRepository).save(appointment);
        assertThat(result).isNotNull();
        assertThat(result.getAppointmentId()).isEqualTo(appointmentId);
        assertThat(result.getState()).isEqualTo(expectedState);
    }

    record CloseScenario(CloseAppointmentCommand.CloseOutcome outcome, boolean sendFollowup, boolean expectEmail) {}

    private static Stream<CloseScenario> closeScenarios() {
        return Stream.of(
                new CloseScenario(CloseAppointmentCommand.CloseOutcome.COMPLETED, true, true),
                new CloseScenario(CloseAppointmentCommand.CloseOutcome.COMPLETED, false, false),
                new CloseScenario(CloseAppointmentCommand.CloseOutcome.NO_SHOW, true, false),
                new CloseScenario(CloseAppointmentCommand.CloseOutcome.NO_SHOW, false, false)
        );
    }

    @Test
    void shouldNotClosePremature() {
        // given
        var appointmentId = UUID.randomUUID();
        var appointment = mock(Appointment.class);
        var timeSlot = mock(TimeSlot.class);
        var date = LocalDate.now().plusDays(1);
        var startTime = LocalTime.of(10, 0);
        var closeOutcome = CloseAppointmentCommand.CloseOutcome.COMPLETED;
        var command = CloseAppointmentCommand.builder()
                .appointmentId(appointmentId)
                .outcome(closeOutcome)
                .sendFollowup(true)
                .build();
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointment.getTimeSlot()).thenReturn(timeSlot);
        when(timeSlot.getSlotDate()).thenReturn(date);
        when(timeSlot.getEndTime()).thenReturn(startTime.plusHours(1));
        when(brandProperties.getTimezone()).thenReturn(String.valueOf(TimeZone.getDefault().toZoneId()));

        // when
        assertThatThrownBy(() -> appointmentService.close(command)).isInstanceOf(PrematureClosureException.class);

        // then
        verify(appointmentRepository).findById(appointmentId);
        verify(brandProperties).getTimezone();
        verify(appointmentRepository, never()).save(any());
        verify(appointmentStateMachine, never()).transition(any(), any(), any());
        verify(emailService, never()).sendFollowUp(any());
    }

    @Test
    void shouldNotCloseWhenAppointmentNotFound() {
        // given
        var appointmentId = UUID.randomUUID();
        var command = CloseAppointmentCommand.builder()
                .appointmentId(appointmentId)
                .outcome(CloseAppointmentCommand.CloseOutcome.COMPLETED)
                .sendFollowup(true)
                .build();
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> appointmentService.close(command)).isInstanceOf(ResourceNotFoundException.class);

        // then
        verify(appointmentRepository).findById(appointmentId);
        verify(brandProperties, never()).getTimezone();
        verify(appointmentRepository, never()).save(any());
        verify(appointmentStateMachine, never()).transition(any(), any(), any());
        verify(emailService, never()).sendFollowUp(any());
    }

    @Test
    void shouldCancel() {
        // given
        var appointmentId = UUID.randomUUID();
        var appointment = mock(Appointment.class);
        var command = CancelAppointmentCommand.builder()
                .appointmentId(appointmentId)
                .reason("Conflict")
                .build();
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointment.getId()).thenReturn(appointmentId);
        when(appointment.getState()).thenReturn(AppointmentState.CANCELLED);

        // when
        var result = appointmentService.cancel(command);

        // then
        verify(appointmentStateMachine).transition(appointment, AppointmentState.CANCELLED, "TUTOR");
        verify(appointmentRepository).save(appointment);
        verify(emailService).sendCancellationNotification(appointment, "Conflict");

        assertThat(result).isNotNull();
        assertThat(result.getAppointmentId()).isEqualTo(appointment.getId());
        assertThat(result.getState()).isEqualTo(appointment.getState());
    }

    @Test
    void shouldNotCancelWhenAppointmentNotFound() {
        // given
        var appointmentId = UUID.randomUUID();
        var command = CancelAppointmentCommand.builder() // not setting reason thus confirming verifyReasonProvided method not called
                .appointmentId(appointmentId)
                .build();
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());
        // when
        assertThatThrownBy(() -> appointmentService.cancel(command)).isInstanceOf(ResourceNotFoundException.class);

        // then
        verify(appointmentRepository).findById(appointmentId);
        verify(appointmentStateMachine, never()).transition(any(), any(), any());
        verify(appointmentRepository, never()).save(any());
        verify(emailService, never()).sendCancellationNotification(any(), any());
    }

    @Test
    void shouldNotCancelWhenReasonNotGiven() {
        // given
        var appointmentId = UUID.randomUUID();
        var command = CancelAppointmentCommand.builder()
                .appointmentId(appointmentId)
                .build();
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(mock(Appointment.class)));

        // when
        assertThatThrownBy(() -> appointmentService.cancel(command)).isInstanceOf(MissingCancellationReasonException.class);

        // then
        verify(appointmentRepository).findById(appointmentId);
        verify(appointmentStateMachine, never()).transition(any(), any(), any());
        verify(appointmentRepository, never()).save(any());
        verify(emailService, never()).sendCancellationNotification(any(), any());
    }
}