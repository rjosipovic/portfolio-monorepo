package com.studioengine.tutor.dataaccess.entities;

import com.studioengine.tutor.dataaccess.enums.AppointmentState;
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
@Table(name = "appointment_state_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppointmentStateLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_state")
    private AppointmentState fromState;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_state", nullable = false)
    private AppointmentState toState;

    @Column(name = "triggered_by", nullable = false)
    private String triggeredBy;

    @Column(name = "transitioned_at", nullable = false)
    private OffsetDateTime transitionedAt;

    public static AppointmentStateLog create(
            Appointment appointment,
            AppointmentState fromState,
            AppointmentState toState,
            String triggeredBy
    ) {
        if (Objects.isNull(appointment)) throw new IllegalArgumentException("appointment must not be null");
        if (Objects.isNull(toState)) throw new IllegalArgumentException("toState must not be null");
        if (Objects.isNull(triggeredBy) || triggeredBy.isBlank()) throw new IllegalArgumentException("triggeredBy must not be blank");
        var entity = new AppointmentStateLog();
        entity.appointment = appointment;
        entity.fromState = fromState;
        entity.toState = toState;
        entity.triggeredBy = triggeredBy;
        entity.transitionedAt = OffsetDateTime.now();
        return entity;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppointmentStateLog that)) return false;
        if (Objects.isNull(this.id) || Objects.isNull(that.id)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.nonNull(id) ? Objects.hashCode(id) : getClass().hashCode();
    }
}
