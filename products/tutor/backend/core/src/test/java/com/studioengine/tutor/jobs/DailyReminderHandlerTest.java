package com.studioengine.tutor.jobs;

import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.NotificationLog;
import com.studioengine.tutor.dataaccess.enums.NotificationType;
import com.studioengine.tutor.dataaccess.repositories.NotificationLogRepository;
import com.studioengine.tutor.email.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyReminderHandlerTest {

    @Mock
    private NotificationLogRepository notificationLogRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private DailyReminderHandler dailyReminderHandler;

    @Test
    void shouldSendReminder() {
        // given
        var appointmentId = UUID.randomUUID();
        var appointment = mock(Appointment.class);
        when(appointment.getId()).thenReturn(appointmentId);
        when(notificationLogRepository.existsByAppointmentIdAndNotificationType(
                appointmentId, NotificationType.REMINDER)).thenReturn(false);

        // when
        dailyReminderHandler.handle(appointment);

        // then
        verify(emailService).sendReminder(appointment);
        var captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository).save(captor.capture());
        assertThat(captor.getValue().getNotificationType()).isEqualTo(NotificationType.REMINDER);
    }

    @Test
    void shouldSkipWhenReminderAlreadySent() {
        // given
        var appointmentId = UUID.randomUUID();
        var appointment = mock(Appointment.class);
        when(appointment.getId()).thenReturn(appointmentId);
        when(notificationLogRepository.existsByAppointmentIdAndNotificationType(
                appointmentId, NotificationType.REMINDER)).thenReturn(true);

        // when
        dailyReminderHandler.handle(appointment);

        // then
        verify(emailService, never()).sendReminder(appointment);
        verify(notificationLogRepository, never()).save(any());
    }
}
