package com.playground.gamification_manager.game.dataaccess.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity(name = "badges")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BadgeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "badge_name", nullable = false, updatable = false)
    private BadgeType badgeType;

    @CreationTimestamp
    @Column(name = "badge_at", nullable = false, updatable = false)
    private ZonedDateTime badgeAt;

    @Version
    @Column(name = "version")
    private long version;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BadgeEntity that)) return false;
        if (Objects.isNull(this.id) || Objects.isNull(that.id)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.nonNull(id) ? Objects.hashCode(id) : getClass().hashCode();
    }

    public static BadgeEntity create(UUID userId, BadgeType badgeType) {
        if (Objects.isNull(userId)) {
            throw new IllegalArgumentException("BadgeEntity userId must not be null");
        }
        if (Objects.isNull(badgeType)) {
            throw new IllegalArgumentException("BadgeEntity badgeType must not be null");
        }
        var badgeEntity = new BadgeEntity();
        badgeEntity.setUserId(userId);
        badgeEntity.setBadgeType(badgeType);
        return badgeEntity;
    }
}
