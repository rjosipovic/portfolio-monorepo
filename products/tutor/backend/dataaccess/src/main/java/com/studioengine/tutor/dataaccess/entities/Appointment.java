package com.studioengine.tutor.dataaccess.entities;

import com.studioengine.tutor.dataaccess.enums.AppointmentOrigin;
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
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "appointment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id", nullable = false)
    private TimeSlot timeSlot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_category_id", nullable = false)
    private ServiceCategory serviceCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentState state;

    @Column(name = "stripe_session_id")
    private String stripeSessionId;

    @Column(name = "session_notes")
    private String sessionNotes;

    @Column(name = "original_price", nullable = false)
    private BigDecimal originalPrice;

    @Column(name = "final_price", nullable = false)
    private BigDecimal finalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentOrigin origin;

    @Column(name = "state_changed_at", nullable = false)
    private OffsetDateTime stateChangedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Version
    private int version;

    public static Appointment create(
            TimeSlot timeSlot,
            ServiceCategory serviceCategory,
            Student student,
            AppointmentState initialState,
            BigDecimal originalPrice,
            BigDecimal finalPrice,
            AppointmentOrigin origin,
            String sessionNotes
    ) {
        if (Objects.isNull(timeSlot)) throw new IllegalArgumentException("timeSlot must not be null");
        if (Objects.isNull(serviceCategory)) throw new IllegalArgumentException("serviceCategory must not be null");
        if (Objects.isNull(student)) throw new IllegalArgumentException("student must not be null");
        if (Objects.isNull(originalPrice)) throw new IllegalArgumentException("originalPrice must not be null");
        if (Objects.isNull(finalPrice)) throw new IllegalArgumentException("finalPrice must not be null");
        var entity = new Appointment();
        entity.timeSlot = timeSlot;
        entity.serviceCategory = serviceCategory;
        entity.student = student;
        entity.state = initialState;
        entity.originalPrice = originalPrice;
        entity.finalPrice = finalPrice;
        entity.origin = origin;
        entity.sessionNotes = sessionNotes;
        entity.stateChangedAt = OffsetDateTime.now();
        return entity;
    }

    public void transitionTo(AppointmentState newState) {
        this.state = newState;
        this.stateChangedAt = OffsetDateTime.now();
    }

    public void updateStripeSessionId(String stripeSessionId) {
        this.stripeSessionId = stripeSessionId;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Appointment that)) return false;
        if (Objects.isNull(this.id) || Objects.isNull(that.id)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.nonNull(id) ? Objects.hashCode(id) : getClass().hashCode();
    }
}

