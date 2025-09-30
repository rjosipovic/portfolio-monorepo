package com.playground.challenge_manager.challenge.dataaccess.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity(name = "challenge_attempts")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChallengeAttemptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "first_number", nullable = false)
    private Integer firstNumber;

    @Column(name = "second_number", nullable = false)
    private Integer secondNumber;

    @Column(name = "result_attempt", nullable = false)
    private Integer resultAttempt;

    @Column(name = "correct", nullable = false)
    private Boolean correct;

    @Column(name = "game", nullable = false)
    private String game;

    @Column(name = "difficulty", nullable = false)
    private String difficulty;

    @CreationTimestamp
    @Column(name = "attempt_date", nullable = false, updatable = false)
    private ZonedDateTime attemptDate;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChallengeAttemptEntity that)) return false;
        if (this.id == null || that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.nonNull(id) ? Objects.hashCode(id) : getClass().hashCode();
    }

    public static ChallengeAttemptEntity create(UUID userId, Integer firstNumber, Integer secondNumber, Integer resultAttempt, Boolean correct, String game, String difficulty) {
        if (Objects.isNull(userId) || Objects.isNull(firstNumber) || Objects.isNull(secondNumber) || Objects.isNull(resultAttempt) || Objects.isNull(correct) || Objects.isNull(game) || Objects.isNull(difficulty)) {
            throw new IllegalArgumentException("Mandatory property must not be null");
        }
        var attempt = new ChallengeAttemptEntity();
        attempt.setUserId(userId);
        attempt.setFirstNumber(firstNumber);
        attempt.setSecondNumber(secondNumber);
        attempt.setResultAttempt(resultAttempt);
        attempt.setCorrect(correct);
        attempt.setGame(game);
        attempt.setDifficulty(difficulty);
        return attempt;
    }
}
