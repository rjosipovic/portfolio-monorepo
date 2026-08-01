package com.studioengine.tutor.jobs;

import com.studioengine.tutor.config.SchedulingProperties;
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

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TutorNudgeHandlerTest {

    @Mock
    private SchedulingProperties schedulingProperties;
    @Mock
    private NotificationLogRepository notificationLogRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private TutorNudgeHandler tutorNudgeHandler;

    @Test
    void shouldSendNudge() {
        // given
        var appointmentId = UUID.randomUUID();
        var appointment = mock(Appointment.class);
        when(appointment.getId()).thenReturn(appointmentId);
        when(schedulingProperties.getNudgeCooldown()).thenReturn(Duration.ofHours(24));
        when(notificationLogRepository.existsByAppointmentIdAndNotificationTypeAndSentAtAfter(
                eq(appointmentId), eq(NotificationType.NUDGE), any())).thenReturn(false);

        // when
        tutorNudgeHandler.handle(appointment);

        // then
        verify(emailService).sendNudge(appointment);
        var captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository).save(captor.capture());
        assertThat(captor.getValue().getNotificationType()).isEqualTo(NotificationType.NUDGE);
    }

    @Test
    void shouldSkipWhenNudgeAlreadySentWithinCooldown() {
        // given
        var appointmentId = UUID.randomUUID();
        var appointment = mock(Appointment.class);
        when(appointment.getId()).thenReturn(appointmentId);
        when(schedulingProperties.getNudgeCooldown()).thenReturn(Duration.ofHours(24));
        when(notificationLogRepository.existsByAppointmentIdAndNotificationTypeAndSentAtAfter(
                eq(appointmentId), eq(NotificationType.NUDGE), any())).thenReturn(true);

        // when
        tutorNudgeHandler.handle(appointment);

        // then
        verify(emailService, never()).sendNudge(appointment);
        verify(notificationLogRepository, never()).save(any());
    }
}
