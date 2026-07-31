package com.studioengine.tutor.jobs;

import com.studioengine.tutor.config.SchedulingProperties;
import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankTransferOverdueJobTest {

    @Mock
    private SchedulingProperties schedulingProperties;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private OverduePaymentHandler overduePaymentHandler;

    @InjectMocks
    private BankTransferOverdueJob bankTransferOverdueJob;

    @Test
    void shouldTriggerOverdueNotifications() {
        // given
        var threshold = Duration.ofHours(48);
        var appointment1 = mock(Appointment.class);
        var appointment2 = mock(Appointment.class);
        when(schedulingProperties.getPaymentOverdueThreshold()).thenReturn(threshold);
        when(appointmentRepository.findOverduePendingPayments(any(OffsetDateTime.class))).thenReturn(List.of(appointment1, appointment2));

        // when
        bankTransferOverdueJob.notifyOverduePayments();

        // then
        verify(schedulingProperties).getPaymentOverdueThreshold();

        var captor = ArgumentCaptor.forClass(OffsetDateTime.class);
        verify(appointmentRepository).findOverduePendingPayments(captor.capture());
        var cutoff = captor.getValue();
        assertThat(Duration.between(cutoff, OffsetDateTime.now())).isGreaterThanOrEqualTo(threshold);

        verify(overduePaymentHandler, times(2)).handle(argThat(a -> a.equals(appointment1) || a.equals(appointment2)));
    }

    @Test
    void shouldNotTriggerWhenNoOverdueAppointments() {
        // given
        var threshold = Duration.ofHours(48);
        when(schedulingProperties.getPaymentOverdueThreshold()).thenReturn(threshold);
        when(appointmentRepository.findOverduePendingPayments(any(OffsetDateTime.class))).thenReturn(List.of());

        // when
        bankTransferOverdueJob.notifyOverduePayments();

        // then
        verify(overduePaymentHandler, never()).handle(any());
    }
}
