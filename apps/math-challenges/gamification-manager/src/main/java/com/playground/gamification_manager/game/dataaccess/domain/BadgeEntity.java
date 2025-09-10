package com.playground.gamification_manager.game.dataaccess.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity(name = "badges")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BadgeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "badge_name")
    @Enumerated(jakarta.persistence.EnumType.STRING)
    private BadgeType badgeType;

    @CreationTimestamp
    @Column(name = "badge_at")
    private ZonedDateTime badgeAt;

}
