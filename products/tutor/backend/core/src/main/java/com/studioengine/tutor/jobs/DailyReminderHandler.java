package com.studioengine.tutor.jobs;

import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.NotificationLog;
import com.studioengine.tutor.dataaccess.enums.NotificationType;
import com.studioengine.tutor.dataaccess.repositories.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyReminderHandler {

    private final NotificationLogRepository notificationLogRepository;

    @Transactional
    public void handle(Appointment appointment) {
        var appointmentId = appointment.getId();
        var alreadySent = notificationLogRepository
                .existsByAppointmentIdAndNotificationType(
                        appointmentId,
                        NotificationType.REMINDER);

        if (alreadySent) {
            log.debug("Skipping appointment {} - reminder already sent", appointmentId);
            return;
        }

        // TODO: sent reminder email to student via EmailService
        var notificationLog = NotificationLog.create(appointment, NotificationType.REMINDER);
        notificationLogRepository.save(notificationLog);
    }
}
