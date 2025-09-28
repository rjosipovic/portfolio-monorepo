package com.playground.challenge_manager.challenge.dataaccess.entities;

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

@Entity(name = "challenge_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeAttemptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(name = "first_number", nullable = false)
    private int firstNumber;
    @Column(name = "second_number", nullable = false)
    private int secondNumber;
    @Column(name = "result_attempt", nullable = false)
    private int resultAttempt;
    @Column(name = "correct", nullable = false)
    private boolean correct;
    @Column(name = "game", nullable = false)
    private String game;
    @Column(name = "difficulty", nullable = false)
    private String difficulty;
    @CreationTimestamp
    @Column(name = "attempt_date", nullable = false)
    private ZonedDateTime attemptDate;
}
