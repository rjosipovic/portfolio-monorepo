package com.studioengine.tutor.jobs;

import com.studioengine.tutor.config.BrandProperties;
import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyReminderJobTest {

    @Mock
    private BrandProperties brandProperties;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private DailyReminderHandler dailyReminderHandler;

    @InjectMocks
    private DailyReminderJob dailyReminderJob;

    @Test
    void shouldTriggerReminders() {
        // given
        var appointment1 = mock(Appointment.class);
        var appointment2 = mock(Appointment.class);
        when(brandProperties.getTimezone()).thenReturn("Europe/Zagreb");
        when(appointmentRepository.findByStatesAndSlotDate(any(), any())).thenReturn(List.of(appointment1, appointment2));

        // when
        dailyReminderJob.sendReminders();

        // then
        verify(appointmentRepository).findByStatesAndSlotDate(any(), any());
        verify(dailyReminderHandler, times(2)).handle(argThat(a -> a.equals(appointment1) || a.equals(appointment2)));
    }

    @Test
    void shouldNotTriggerWhenNoAppointmentsForToday() {
        // given
        when(brandProperties.getTimezone()).thenReturn("Europe/Zagreb");
        when(appointmentRepository.findByStatesAndSlotDate(any(), any())).thenReturn(List.of());

        // when
        dailyReminderJob.sendReminders();

        // then
        verify(dailyReminderHandler, never()).handle(any());
    }
}
