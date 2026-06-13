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
@Table(name = "student")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phone;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public static Student create(String name, String email, String phone) {
        if (Objects.isNull(name) || name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        if (Objects.isNull(email) || email.isBlank()) throw new IllegalArgumentException("email must not be blank");
        if (Objects.isNull(phone) || phone.isBlank()) throw new IllegalArgumentException("phone must not be blank");
        var entity = new Student();
        entity.name = name;
        entity.email = email;
        entity.phone = phone;
        return entity;
    }

    public void updateDetails(String name, String email, String phone) {
        if (Objects.isNull(name) || name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        if (Objects.isNull(email) || email.isBlank()) throw new IllegalArgumentException("email must not be blank");
        if (Objects.isNull(phone) || phone.isBlank()) throw new IllegalArgumentException("phone must not be blank");
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student that)) return false;
        if (Objects.isNull(this.id) || Objects.isNull(that.id)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.nonNull(id) ? Objects.hashCode(id) : getClass().hashCode();
    }
}
