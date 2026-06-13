package com.studioengine.tutor.dataaccess.entities;

import com.studioengine.tutor.dataaccess.enums.PaymentMethod;
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
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "payment_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "stripe_payment_id")
    private String stripePaymentId;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @CreationTimestamp
    @Column(name = "recorded_at", nullable = false, updatable = false)
    private OffsetDateTime recordedAt;

    public static PaymentRecord create(
            Appointment appointment,
            BigDecimal amount,
            PaymentMethod paymentMethod,
            LocalDate paymentDate,
            String stripePaymentId
    ) {
        if (Objects.isNull(appointment)) throw new IllegalArgumentException("appointment must not be null");
        if (Objects.isNull(amount)) throw new IllegalArgumentException("amount must not be null");
        if (Objects.isNull(paymentMethod)) throw new IllegalArgumentException("paymentMethod must not be null");
        if (Objects.isNull(paymentDate)) throw new IllegalArgumentException("paymentDate must not be null");
        var entity = new PaymentRecord();
        entity.appointment = appointment;
        entity.amount = amount;
        entity.paymentMethod = paymentMethod;
        entity.paymentDate = paymentDate;
        entity.stripePaymentId = stripePaymentId;
        return entity;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaymentRecord that)) return false;
        if (Objects.isNull(this.id) || Objects.isNull(that.id)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.nonNull(id) ? Objects.hashCode(id) : getClass().hashCode();
    }
}
