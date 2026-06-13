package com.studioengine.tutor.dataaccess.entities;

import com.studioengine.tutor.dataaccess.enums.TimeSlotState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "time_slot")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimeSlotState state;

    @Column(name = "state_changed_at", nullable = false)
    private OffsetDateTime stateChangedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Version
    private int version;

    public static TimeSlot create(LocalDate slotDate, LocalTime startTime) {
        if (Objects.isNull(slotDate)) throw new IllegalArgumentException("slotDate must not be null");
        if (Objects.isNull(startTime)) throw new IllegalArgumentException("startTime must not be null");
        var entity = new TimeSlot();
        entity.slotDate = slotDate;
        entity.startTime = startTime;
        entity.endTime = startTime.plusHours(1);
        entity.state = TimeSlotState.DRAFT;
        entity.stateChangedAt = OffsetDateTime.now();
        return entity;
    }

    public void transitionTo(TimeSlotState newState) {
        this.state = newState;
        this.stateChangedAt = OffsetDateTime.now();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeSlot that)) return false;
        if (Objects.isNull(this.id) || Objects.isNull(that.id)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.nonNull(id) ? Objects.hashCode(id) : getClass().hashCode();
    }
}
