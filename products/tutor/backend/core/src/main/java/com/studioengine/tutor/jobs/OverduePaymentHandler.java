package com.studioengine.tutor.jobs;

import com.studioengine.tutor.config.SchedulingProperties;
import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.NotificationLog;
import com.studioengine.tutor.dataaccess.enums.NotificationType;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import com.studioengine.tutor.dataaccess.repositories.NotificationLogRepository;
import com.studioengine.tutor.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OverduePaymentHandler {

    private final AppointmentRepository appointmentRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final EmailService emailService;
    private final SchedulingProperties schedulingProperties;
    private final Clock clock;

    @Transactional
    public void handle(UUID appointmentId) {
        var appointment = findAppointment(appointmentId);
        var cooldown = OffsetDateTime.now(clock).minus(schedulingProperties.getPaymentOverdueNotificationCooldown());

        var tutorNotified = notificationLogRepository
                .existsByAppointmentIdAndNotificationTypeAndSentAtAfter(
                        appointment.getId(),
                        NotificationType.OVERDUE_TUTOR,
                        cooldown);

        var studentNotified = notificationLogRepository
                .existsByAppointmentIdAndNotificationTypeAndSentAtAfter(
                        appointment.getId(),
                        NotificationType.OVERDUE_STUDENT,
                        cooldown);

        if (tutorNotified && studentNotified) {
            log.debug("Skipping appointment {} - already notified within cooldown window", appointmentId);
            return;
        }

        if (!tutorNotified) {
            emailService.sendOverdueNotificationToTutor(appointment);
            var notificationLog = NotificationLog.create(appointment, NotificationType.OVERDUE_TUTOR);
            notificationLogRepository.save(notificationLog);
            log.info("Sent overdue notification to tutor for appointment {}", appointmentId);
        }

        if (!studentNotified) {
            emailService.sendOverdueNotificationToStudent(appointment);
            var notificationLog = NotificationLog.create(appointment, NotificationType.OVERDUE_STUDENT);
            notificationLogRepository.save(notificationLog);
            log.info("Sent overdue notification to student for appointment {}", appointmentId);
        }
    }

    private Appointment findAppointment(UUID appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalStateException("Appointment not found: " + appointmentId));
    }
}
