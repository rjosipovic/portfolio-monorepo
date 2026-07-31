package com.studioengine.tutor.jobs;

import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.NotificationLog;
import com.studioengine.tutor.dataaccess.enums.NotificationType;
import com.studioengine.tutor.dataaccess.repositories.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class OverduePaymentHandler {

    private final NotificationLogRepository notificationLogRepository;

    @Transactional
    public void handle(Appointment appointment) {
        var appointmentId = appointment.getId();
        var cooldown = OffsetDateTime.now().minusHours(24);

        var tutorNotified = notificationLogRepository
                .existsByAppointmentIdAndNotificationTypeAndSentAtAfter(
                        appointmentId,
                        NotificationType.OVERDUE_TUTOR,
                        cooldown);

        var studentNotified = notificationLogRepository
                .existsByAppointmentIdAndNotificationTypeAndSentAtAfter(
                        appointmentId,
                        NotificationType.OVERDUE_STUDENT,
                        cooldown);

        if (tutorNotified && studentNotified) {
            log.debug("Skipping appointment {} - already notified within 24h", appointmentId);
            return;
        }

        if (!tutorNotified) {
            // TODO: send overdue notification to tutor via EmailService
            var notificationLog = NotificationLog.create(appointment, NotificationType.OVERDUE_TUTOR);
            notificationLogRepository.save(notificationLog);
            log.info("Sent overdue notification to tutor for appointment {}", appointmentId);
        }

        if (!studentNotified) {
            // TODO: send overdue notification to student via EmailService
            var notificationLog = NotificationLog.create(appointment, NotificationType.OVERDUE_STUDENT);
            notificationLogRepository.save(notificationLog);
            log.info("Sent overdue notification to student for appointment {}", appointmentId);
        }
    }
}
