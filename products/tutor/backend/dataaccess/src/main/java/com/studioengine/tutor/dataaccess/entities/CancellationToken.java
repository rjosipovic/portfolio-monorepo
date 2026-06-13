package com.studioengine.tutor.dataaccess.entities;

import com.studioengine.tutor.dataaccess.enums.TokenType;
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

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "cancellation_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CancellationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Column(nullable = false, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false)
    private TokenType tokenType;

    @Column(nullable = false)
    private boolean used;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public static CancellationToken create(Appointment appointment, TokenType tokenType, OffsetDateTime expiresAt) {
        if (Objects.isNull(appointment)) throw new IllegalArgumentException("appointment must not be null");
        if (Objects.isNull(tokenType)) throw new IllegalArgumentException("tokenType must not be null");
        if (Objects.isNull(expiresAt)) throw new IllegalArgumentException("expiresAt must not be null");
        var entity = new CancellationToken();
        entity.appointment = appointment;
        entity.token = UUID.randomUUID().toString();
        entity.tokenType = tokenType;
        entity.used = false;
        entity.expiresAt = expiresAt;
        return entity;
    }

    public void markUsed() {
        this.used = true;
    }

    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresAt);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CancellationToken that)) return false;
        if (Objects.isNull(this.id) || Objects.isNull(that.id)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.nonNull(id) ? Objects.hashCode(id) : getClass().hashCode();
    }
}
