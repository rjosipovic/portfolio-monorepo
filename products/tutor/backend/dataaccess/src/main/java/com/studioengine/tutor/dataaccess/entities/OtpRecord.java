package com.studioengine.tutor.dataaccess.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "otp_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OtpRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String email;

    @Column(name = "otp_hash", nullable = false)
    private String otpHash;

    @Column(nullable = false)
    private boolean used;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public static OtpRecord create(String email, String otpHash, OffsetDateTime expiresAt) {
        if (Objects.isNull(email) || email.isBlank()) throw new IllegalArgumentException("email must not be blank");
        if (Objects.isNull(otpHash) || otpHash.isBlank()) throw new IllegalArgumentException("otpHash must not be blank");
        if (Objects.isNull(expiresAt)) throw new IllegalArgumentException("expiresAt must not be null");
        var entity = new OtpRecord();
        entity.email = email;
        entity.otpHash = otpHash;
        entity.used = false;
        entity.expiresAt = expiresAt;
        return entity;
    }

    public void markUsed() {
        this.used = true;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OtpRecord that)) return false;
        if (Objects.isNull(this.id) || Objects.isNull(that.id)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.nonNull(id) ? Objects.hashCode(id) : getClass().hashCode();
    }
}

