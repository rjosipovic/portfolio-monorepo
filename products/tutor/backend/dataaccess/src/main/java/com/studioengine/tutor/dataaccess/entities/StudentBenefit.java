package com.studioengine.tutor.dataaccess.entities;

import com.studioengine.tutor.dataaccess.enums.BenefitType;
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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "student_benefit")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentBenefit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BenefitType type;

    @Column(nullable = false)
    private BigDecimal value;

    private String note;

    @Column(nullable = false)
    private boolean consumed;

    @Column(name = "granted_at", nullable = false)
    private OffsetDateTime grantedAt;

    @Column(name = "consumed_at")
    private OffsetDateTime consumedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumed_by_appointment_id")
    private Appointment consumedByAppointment;

    public static StudentBenefit create(Student student, BenefitType type, BigDecimal value, String note) {
        if (Objects.isNull(student)) throw new IllegalArgumentException("student must not be null");
        if (Objects.isNull(type)) throw new IllegalArgumentException("type must not be null");
        if (type != BenefitType.FREE_LESSON && (Objects.isNull(value) || value.compareTo(BigDecimal.ZERO) <= 0 )) throw new IllegalArgumentException("value is required for " + type);
        var entity = new StudentBenefit();
        entity.student = student;
        entity.type = type;
        entity.value = value;
        entity.note = note;
        entity.consumed = false;
        entity.grantedAt = OffsetDateTime.now();
        return entity;
    }

    public void consume(Appointment appointment) {
        this.consumed = true;
        this.consumedAt = OffsetDateTime.now();
        this.consumedByAppointment = appointment;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StudentBenefit that)) return false;
        if (Objects.isNull(this.id) || Objects.isNull(that.id)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.nonNull(id) ? Objects.hashCode(id) : getClass().hashCode();
    }
}
