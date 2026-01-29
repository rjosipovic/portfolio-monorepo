package com.playground.challenge_manager.challenge.dataaccess.entities;

import com.playground.challenge_manager.challenge.dataaccess.converters.IntegerListConverter;
import com.playground.challenge_manager.challenge.services.model.ChallengeStatus;
import com.playground.challenge_manager.challenge.services.model.DifficultyLevel;
import com.playground.challenge_manager.challenge.services.model.OperationType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity(name = "challenge")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChallengeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ChallengeStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation", nullable = false, updatable = false)
    private OperationType operationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, updatable = false)
    private DifficultyLevel difficulty;

    @Column(name = "operands", nullable = false, updatable = false, columnDefinition = "TEXT")
    @Convert(converter = IntegerListConverter.class)
    private List<Integer> operands;

    @Column(name = "correct_answer")
    private Integer correctAnswer;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "expires_at")
    private ZonedDateTime expiresAt;

    @Column(name = "attempted_at")
    private ZonedDateTime attemptedAt;

    public void updateCorrectAnswer(Integer correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public void updateExpiresAt(ZonedDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void updateAttemptedAt(ZonedDateTime attemptedAt) {
        this.attemptedAt = attemptedAt;
    }

    public void updateStatus(ChallengeStatus status) {
        this.status = status;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChallengeEntity that)) return false;
        if (this.id == null || that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.nonNull(id) ? Objects.hashCode(id) : getClass().hashCode();
    }

    public static ChallengeEntity create(List<Integer> operands, OperationType operationType, DifficultyLevel difficultyLevel, UUID userId) {
        validateParameters(operands, operationType, difficultyLevel, userId);

        var challenge = new ChallengeEntity();
        challenge.status = ChallengeStatus.GENERATED;
        challenge.operands = operands;
        challenge.operationType = operationType;
        challenge.difficulty = difficultyLevel;
        challenge.userId = userId;
        return challenge;
    }

    private static void validateParameters(List<Integer> operands, OperationType operationType, DifficultyLevel difficultyLevel, UUID userId) {
        if (Objects.isNull(operationType)) {
            throw new IllegalArgumentException("OperationType must not be null.");
        }

        if (Objects.isNull(difficultyLevel)) {
            throw new IllegalArgumentException("DifficultyLevel must not be null.");
        }

        if (Objects.isNull(operands) || operands.isEmpty() || operands.size() < 2) {
            throw new IllegalArgumentException("Operands list must not be null and must contain at least 2 numbers.");
        }

        if (Objects.isNull(userId)) {
            throw new IllegalArgumentException("UserId must not be null.");
        }
    }
}
