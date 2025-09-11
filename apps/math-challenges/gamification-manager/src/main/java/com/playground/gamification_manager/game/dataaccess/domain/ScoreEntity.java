package com.playground.gamification_manager.game.dataaccess.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity(name = "scores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "challenge_attempt_id")
    private UUID challengeAttemptId;

    @Column(name = "score")
    private int score;

    @CreationTimestamp
    @Column(name = "score_at")
    private ZonedDateTime scoreAt;
}
