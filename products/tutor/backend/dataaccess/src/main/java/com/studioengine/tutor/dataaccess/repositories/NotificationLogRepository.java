package com.studioengine.tutor.dataaccess.repositories;

import com.studioengine.tutor.dataaccess.entities.NotificationLog;
import com.studioengine.tutor.dataaccess.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    boolean existsByAppointmentIdAndNotificationType(UUID appointmentId, NotificationType notificationType);

    boolean existsByAppointmentIdAndNotificationTypeAndSentAtAfter(UUID appointmentId, NotificationType notificationType, OffsetDateTime after);
}
