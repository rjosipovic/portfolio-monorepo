package com.playground.gamification_manager.game.dataaccess.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity(name = "scores")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @NotNull
    @Column(name = "challenge_attempt_id", nullable = false, updatable = false)
    private UUID challengeAttemptId;

    @Positive
    @Column(name = "score", nullable = false, updatable = false)
    private int score;

    @CreationTimestamp
    @Column(name = "score_at", nullable = false, updatable = false)
    private ZonedDateTime scoreAt;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScoreEntity that)) return false;
        if (Objects.isNull(this.id) || Objects.isNull(that.id)) return false;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.nonNull(id) ? Objects.hashCode(id) : getClass().hashCode();
    }

    public static ScoreEntity create(UUID userId, UUID challengeAttemptId, int score) {
        if (Objects.isNull(userId)) {
            throw new IllegalArgumentException("ScoreEntity userId must not be null");
        }
        if (Objects.isNull(challengeAttemptId)) {
            throw new IllegalArgumentException("ScoreEntity challengeAttemptId must not be null");
        }
        if (score <= 0) {
            throw new IllegalArgumentException("ScoreEntity score must be a positive value");
        }
        var scoreEntity = new ScoreEntity();
        scoreEntity.setUserId(userId);
        scoreEntity.setChallengeAttemptId(challengeAttemptId);
        scoreEntity.setScore(score);
        return scoreEntity;
    }
}
