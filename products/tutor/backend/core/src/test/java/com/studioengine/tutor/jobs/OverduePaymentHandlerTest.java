package com.studioengine.tutor.jobs;

import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.NotificationLog;
import com.studioengine.tutor.dataaccess.enums.NotificationType;
import com.studioengine.tutor.dataaccess.repositories.NotificationLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OverduePaymentHandlerTest {

    @Mock
    private NotificationLogRepository notificationLogRepository;

    @InjectMocks
    private OverduePaymentHandler overduePaymentHandler;

    @Test
    void shouldNotifyBothTutorAndStudent() {
        // given
        var appointmentId = UUID.randomUUID();
        var appointment = mock(Appointment.class);
        when(appointment.getId()).thenReturn(appointmentId);
        when(notificationLogRepository.existsByAppointmentIdAndNotificationTypeAndSentAtAfter(
                eq(appointmentId), eq(NotificationType.OVERDUE_TUTOR), any())).thenReturn(false);
        when(notificationLogRepository.existsByAppointmentIdAndNotificationTypeAndSentAtAfter(
                eq(appointmentId), eq(NotificationType.OVERDUE_STUDENT), any())).thenReturn(false);

        // when
        overduePaymentHandler.handle(appointment);

        // then
        var captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository, times(2)).save(captor.capture());
        var savedLogs = captor.getAllValues();
        assertThat(savedLogs).hasSize(2);
        assertThat(savedLogs.get(0).getNotificationType()).isEqualTo(NotificationType.OVERDUE_TUTOR);
        assertThat(savedLogs.get(1).getNotificationType()).isEqualTo(NotificationType.OVERDUE_STUDENT);
    }

    @Test
    void shouldNotifyOnlyStudent() {
        // given
        var appointmentId = UUID.randomUUID();
        var appointment = mock(Appointment.class);
        when(appointment.getId()).thenReturn(appointmentId);
        when(notificationLogRepository.existsByAppointmentIdAndNotificationTypeAndSentAtAfter(
                eq(appointmentId), eq(NotificationType.OVERDUE_TUTOR), any())).thenReturn(true);
        when(notificationLogRepository.existsByAppointmentIdAndNotificationTypeAndSentAtAfter(
                eq(appointmentId), eq(NotificationType.OVERDUE_STUDENT), any())).thenReturn(false);

        // when
        overduePaymentHandler.handle(appointment);

        // then
        var captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getNotificationType()).isEqualTo(NotificationType.OVERDUE_STUDENT);
    }

    @Test
    void shouldNotifyOnlyTutor() {
        // given
        var appointmentId = UUID.randomUUID();
        var appointment = mock(Appointment.class);
        when(appointment.getId()).thenReturn(appointmentId);
        when(notificationLogRepository.existsByAppointmentIdAndNotificationTypeAndSentAtAfter(
                eq(appointmentId), eq(NotificationType.OVERDUE_TUTOR), any())).thenReturn(false);
        when(notificationLogRepository.existsByAppointmentIdAndNotificationTypeAndSentAtAfter(
                eq(appointmentId), eq(NotificationType.OVERDUE_STUDENT), any())).thenReturn(true);

        // when
        overduePaymentHandler.handle(appointment);

        // then
        var captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getNotificationType()).isEqualTo(NotificationType.OVERDUE_TUTOR);
    }

    @Test
    void shouldSkipWhenBothAlreadyNotified() {
        // given
        var appointmentId = UUID.randomUUID();
        var appointment = mock(Appointment.class);
        when(appointment.getId()).thenReturn(appointmentId);
        when(notificationLogRepository.existsByAppointmentIdAndNotificationTypeAndSentAtAfter(
                eq(appointmentId), eq(NotificationType.OVERDUE_TUTOR), any())).thenReturn(true);
        when(notificationLogRepository.existsByAppointmentIdAndNotificationTypeAndSentAtAfter(
                eq(appointmentId), eq(NotificationType.OVERDUE_STUDENT), any())).thenReturn(true);

        // when
        overduePaymentHandler.handle(appointment);

        // then
        verify(notificationLogRepository, never()).save(any());
    }
}
