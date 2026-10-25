package com.studioengine.tutor.jobs;

import com.studioengine.tutor.config.SchedulingProperties;
import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.NotificationLog;
import com.studioengine.tutor.dataaccess.enums.NotificationType;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import com.studioengine.tutor.dataaccess.repositories.NotificationLogRepository;
import com.studioengine.tutor.email.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OverduePaymentHandlerTest {

    private static final String ZONE_ID = "Europe/Zagreb";
    private static final int YEAR_NOW = 2026;
    private static final int MONTH_NOW = 9;
    private static final int DAY_NOW = 22;
    private static final int HOUR_NOW = 12;
    private static final int MIN_NOW = 0;

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private NotificationLogRepository notificationLogRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private SchedulingProperties schedulingProperties;

    private Clock clock;

    private OverduePaymentHandler overduePaymentHandler;

    @BeforeEach
    void setUp() {
        var zone = ZoneId.of(ZONE_ID);
        var fixedNow = LocalDateTime.of(YEAR_NOW, MONTH_NOW, DAY_NOW, HOUR_NOW, MIN_NOW).atZone(zone).toInstant();
        clock = Clock.fixed(fixedNow, zone);
        overduePaymentHandler = new OverduePaymentHandler(appointmentRepository, notificationLogRepository, emailService, schedulingProperties, clock);
    }

    @Test
    void shouldNotifyBothTutorAndStudent() {
        // given
        var appointmentId = UUID.randomUUID();
        var appointment = mock(Appointment.class);
        var paymentOverdueNotificationCooldown = Duration.ofHours(24);
        var cooldown = OffsetDateTime.now(clock).minus(paymentOverdueNotificationCooldown);
        when(appointment.getId()).thenReturn(appointmentId);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(schedulingProperties.getPaymentOverdueNotificationCooldown()).thenReturn(paymentOverdueNotificationCooldown);
        when(notificationLogRepository.existsByAppointmentIdAndNotificationTypeAndSentAtAfter(
                eq(appointmentId), eq(NotificationType.OVERDUE_TUTOR), eq(cooldown))).thenReturn(false);
        when(notificationLogRepository.existsByAppointmentIdAndNotificationTypeAndSentAtAfter(
                eq(appointmentId), eq(NotificationType.OVERDUE_STUDENT), eq(cooldown))).thenReturn(false);

        // when
        overduePaymentHandler.handle(appointmentId);

        // then
        verify(appointmentRepository).findById(appointmentId);
        verify(schedulingProperties).getPaymentOverdueNotificationCooldown();
        verify(notificationLogRepository).existsByAppointmentIdAndNotificationTypeAndSentAtAfter(eq(appointmentId), eq(NotificationType.OVERDUE_TUTOR), eq(cooldown));
        verify(notificationLogRepository).existsByAppointmentIdAndNotificationTypeAndSentAtAfter(eq(appointmentId), eq(NotificationType.OVERDUE_STUDENT), eq(cooldown));
        verify(emailService).sendOverdueNotificationToTutor(appointment);
        verify(emailService).sendOverdueNotificationToStudent(appointment);
        var captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository, times(2)).save(captor.capture());
        var savedLogs = captor.getAllValues();
        assertThat(savedLogs).hasSize(2);
        assertThat(savedLogs.get(0).getNotificationType()).isEqualTo(NotificationType.OVERDUE_TUTOR);
        assertThat(savedLogs.get(1).getNotificationType()).isEqualTo(NotificationType.OVERDUE_STUDENT);
    }

    @Test
    void shouldNotNotifyWhenAppointmentNotExists() {
        // given
        var appointmentId = UUID.randomUUID();
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> overduePaymentHandler.handle(appointmentId)).isInstanceOf(IllegalStateException.class);

        // then
        verify(appointmentRepository).findById(appointmentId);
        verify(schedulingProperties, never()).getPaymentOverdueNotificationCooldown();
        verify(emailService, never()).sendOverdueNotificationToTutor(any());
        verify(emailService, never()).sendOverdueNotificationToStudent(any());
    }

    @Test
    void shouldNotifyOnlyStudent() {
        // given
        var appointmentId = UUID.randomUUID();
        var appointment = mock(Appointment.class);
        var paymentOverdueNotificationCooldown = Duration.ofHours(24);
        var cooldown = OffsetDateTime.now(clock).minus(paymentOverdueNotificationCooldown);
        when(appointment.getId()).thenReturn(appointmentId);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(schedulingProperties.getPaymentOverdueNotificationCooldown()).thenReturn(paymentOverdueNotificationCooldown);
        when(notificationLogRepository.existsByAppointmentIdAndNotificationTypeAndSentAtAfter(
                eq(appointmentId), eq(NotificationType.OVERDUE_TUTOR), eq(cooldown))).thenReturn(true);
        when(notificationLogRepository.existsByAppointmentIdAndNotificationTypeAndSentAtAfter(
                eq(appointmentId), eq(NotificationType.OVERDUE_STUDENT), eq(cooldown))).thenReturn(false);

        // when
        overduePaymentHandler.handle(appointmentId);

        // then
        verify(appointmentRepository).findById(appointmentId);
        verify(schedulingProperties).getPaymentOverdueNotificationCooldown();
        verify(notificationLogRepository).existsByAppointmentIdAndNotificationTypeAndSentAtAfter(eq(appointmentId), eq(NotificationType.OVERDUE_TUTOR), eq(cooldown));
        verify(notificationLogRepository).existsByAppointmentIdAndNotificationTypeAndSentAtAfter(eq(appointmentId), eq(NotificationType.OVERDUE_STUDENT), eq(cooldown));
        verify(emailService, never()).sendOverdueNotificationToTutor(appointment);
        verify(emailService).sendOverdueNotificationToStudent(appointment);
        var captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getNotificationType()).isEqualTo(NotificationType.OVERDUE_STUDENT);
    }

    @Test
    void shouldNotifyOnlyTutor() {
        // given
        var appointmentId = UUID.randomUUID();
        var appointment = mock(Appointment.class);
        var paymentOverdueNotificationCooldown = Duration.ofHours(24);
        var cooldown = OffsetDateTime.now(clock).minus(paymentOverdueNotificationCooldown);
        when(appointment.getId()).thenReturn(appointmentId);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(schedulingProperties.getPaymentOverdueNotificationCooldown()).thenReturn(paymentOverdueNotificationCooldown);
        when(notificationLogRepository.existsByAppointmentIdAndNotificationTypeAndSentAtAfter(
                eq(appointmentId), eq(NotificationType.OVERDUE_TUTOR), eq(cooldown))).thenReturn(false);
        when(notificationLogRepository.existsByAppointmentIdAndNotificationTypeAndSentAtAfter(
                eq(appointmentId), eq(NotificationType.OVERDUE_STUDENT), eq(cooldown))).thenReturn(true);

        // when
        overduePaymentHandler.handle(appointmentId);

        // then
        verify(appointmentRepository).findById(appointmentId);
        verify(schedulingProperties).getPaymentOverdueNotificationCooldown();
        verify(notificationLogRepository).existsByAppointmentIdAndNotificationTypeAndSentAtAfter(eq(appointmentId), eq(NotificationType.OVERDUE_TUTOR), eq(cooldown));
        verify(notificationLogRepository).existsByAppointmentIdAndNotificationTypeAndSentAtAfter(eq(appointmentId), eq(NotificationType.OVERDUE_STUDENT), eq(cooldown));
        verify(emailService).sendOverdueNotificationToTutor(appointment);
        verify(emailService, never()).sendOverdueNotificationToStudent(appointment);
        var captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getNotificationType()).isEqualTo(NotificationType.OVERDUE_TUTOR);
    }

    @Test
    void shouldSkipWhenBothAlreadyNotified() {
        // given
        var appointmentId = UUID.randomUUID();
        var appointment = mock(Appointment.class);
        var paymentOverdueNotificationCooldown = Duration.ofHours(24);
        var cooldown = OffsetDateTime.now(clock).minus(paymentOverdueNotificationCooldown);
        when(appointment.getId()).thenReturn(appointmentId);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(schedulingProperties.getPaymentOverdueNotificationCooldown()).thenReturn(paymentOverdueNotificationCooldown);
        when(notificationLogRepository.existsByAppointmentIdAndNotificationTypeAndSentAtAfter(
                eq(appointmentId), eq(NotificationType.OVERDUE_TUTOR), eq(cooldown))).thenReturn(true);
        when(notificationLogRepository.existsByAppointmentIdAndNotificationTypeAndSentAtAfter(
                eq(appointmentId), eq(NotificationType.OVERDUE_STUDENT), eq(cooldown))).thenReturn(true);

        // when
        overduePaymentHandler.handle(appointmentId);

        // then
        verify(appointmentRepository).findById(appointmentId);
        verify(schedulingProperties).getPaymentOverdueNotificationCooldown();
        verify(notificationLogRepository).existsByAppointmentIdAndNotificationTypeAndSentAtAfter(eq(appointmentId), eq(NotificationType.OVERDUE_TUTOR), eq(cooldown));
        verify(notificationLogRepository).existsByAppointmentIdAndNotificationTypeAndSentAtAfter(eq(appointmentId), eq(NotificationType.OVERDUE_STUDENT), eq(cooldown));
        verify(emailService, never()).sendOverdueNotificationToTutor(appointment);
        verify(emailService, never()).sendOverdueNotificationToStudent(appointment);
        verify(notificationLogRepository, never()).save(any());
    }
}
