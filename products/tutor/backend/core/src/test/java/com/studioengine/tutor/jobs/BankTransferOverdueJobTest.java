package com.studioengine.tutor.jobs;

import com.studioengine.tutor.config.BrandProperties;
import com.studioengine.tutor.config.SchedulingProperties;
import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

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

    private static final String ZONE_ID = "Europe/Zagreb";
    private static final int YEAR_NOW = 2026;
    private static final int MONTH_NOW = 9;
    private static final int DAY_NOW = 22;
    private static final int HOUR_NOW = 12;
    private static final int MIN_NOW = 0;

    @Mock
    private SchedulingProperties schedulingProperties;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private OverduePaymentHandler overduePaymentHandler;

    @Mock
    private BrandProperties brandProperties;

    private BankTransferOverdueJob bankTransferOverdueJob;

    @BeforeEach
    void setUp() {
        var zone = ZoneId.of(ZONE_ID);
        var fixedNow = LocalDateTime.of(YEAR_NOW, MONTH_NOW, DAY_NOW, HOUR_NOW, MIN_NOW).atZone(zone).toInstant();
        var clock = Clock.fixed(fixedNow, zone);
        bankTransferOverdueJob = new BankTransferOverdueJob(schedulingProperties, appointmentRepository, overduePaymentHandler, clock, brandProperties);
    }

    @Test
    void shouldTriggerOverdueNotifications() {
        // given
        var threshold = Duration.ofHours(48);
        var appointmentId1 = UUID.randomUUID();
        var appointmentId2 = UUID.randomUUID();
        var appointment1 = mock(Appointment.class);
        var appointment2 = mock(Appointment.class);
        when(appointment1.getId()).thenReturn(appointmentId1);
        when(appointment2.getId()).thenReturn(appointmentId2);
        when(schedulingProperties.getPaymentOverdueThreshold()).thenReturn(threshold);
        when(brandProperties.getTimezone()).thenReturn(ZONE_ID);
        when(appointmentRepository.findOverduePendingPayments(any(OffsetDateTime.class), any(LocalDate.class), any(LocalTime.class))).thenReturn(List.of(appointment1, appointment2));

        // when
        bankTransferOverdueJob.notifyOverduePayments();

        // then
        verify(schedulingProperties).getPaymentOverdueThreshold();

        var cutoffCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);
        var dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        var timeCaptor = ArgumentCaptor.forClass(LocalTime.class);
        verify(appointmentRepository).findOverduePendingPayments(
                cutoffCaptor.capture(),
                dateCaptor.capture(),
                timeCaptor.capture()
        );
        var fixedNow = LocalDateTime.of(YEAR_NOW, MONTH_NOW, DAY_NOW, HOUR_NOW, MIN_NOW).atZone(ZoneId.of(ZONE_ID));

        var expectedCutoff = fixedNow.toOffsetDateTime().minus(threshold);
        assertThat(cutoffCaptor.getValue().toInstant()).isEqualTo(expectedCutoff.toInstant());

        assertThat(dateCaptor.getValue()).isEqualTo(LocalDate.of(YEAR_NOW, MONTH_NOW, DAY_NOW));
        assertThat(timeCaptor.getValue()).isEqualTo(LocalTime.of(HOUR_NOW, MIN_NOW));
        verify(overduePaymentHandler, times(2)).handle(argThat(a -> a.equals(appointment1.getId()) || a.equals(appointment2.getId())));
    }

    @Test
    void shouldNotTriggerWhenNoOverdueAppointments() {
        // given
        var threshold = Duration.ofHours(48);
        when(schedulingProperties.getPaymentOverdueThreshold()).thenReturn(threshold);
        when(brandProperties.getTimezone()).thenReturn(ZONE_ID);
        when(appointmentRepository.findOverduePendingPayments(any(OffsetDateTime.class), any(LocalDate.class), any(LocalTime.class))).thenReturn(List.of());

        // when
        bankTransferOverdueJob.notifyOverduePayments();

        // then
        verify(overduePaymentHandler, never()).handle(any());
    }
}
