package com.playground.challenge_manager.challenge.services.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.ZonedDateTime;
import java.util.UUID;


@Value
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChallengeAttempt {

    UUID challengeAttemptId;
    UUID userId;
    Integer firstNumber;
    Integer secondNumber;
    Integer resultAttempt;
    Boolean correct;
    String game;
    String difficulty;
    ZonedDateTime attemptDate;
}
