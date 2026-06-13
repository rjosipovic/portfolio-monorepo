package com.studioengine.tutor.dataaccess.entities;

import com.studioengine.tutor.dataaccess.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "notification_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    @Column(name = "sent_at", nullable = false)
    private OffsetDateTime sentAt;

    public static NotificationLog create(Appointment appointment, NotificationType notificationType) {
        if (Objects.isNull(appointment)) throw new IllegalArgumentException("appointment must not be null");
        if (Objects.isNull(notificationType)) throw new IllegalArgumentException("notificationType must not be null");
        var entity = new NotificationLog();
        entity.appointment = appointment;
        entity.notificationType = notificationType;
        entity.sentAt = OffsetDateTime.now();
        return entity;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificationLog that)) return false;
        if (Objects.isNull(this.id) || Objects.isNull(that.id)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.nonNull(id) ? Objects.hashCode(id) : getClass().hashCode();
    }
}

