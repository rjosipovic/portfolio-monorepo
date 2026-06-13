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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "service_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private boolean active;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public static ServiceCategory create(String name, String description, BigDecimal price, String currency) {
        if (Objects.isNull(name) || name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        if (Objects.isNull(price)) throw new IllegalArgumentException("price must not be null");
        if (Objects.isNull(currency) || currency.isBlank()) throw new IllegalArgumentException("currency must not be blank");
        var entity = new ServiceCategory();
        entity.name = name;
        entity.description = description;
        entity.price = price;
        entity.currency = currency;
        entity.active = true;
        return entity;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceCategory that)) return false;
        if (Objects.isNull(this.id) || Objects.isNull(that.id)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.nonNull(id) ? Objects.hashCode(id) : getClass().hashCode();
    }
}
