package com.playground.challenge_manager.challenge.messaging.events;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.ZonedDateTime;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChallengeSolvedEvent {

    String userId;
    String challengeAttemptId;
    int firstNumber;
    int secondNumber;
    int resultAttempt;
    boolean correct;
    String game;
    String difficulty;
    ZonedDateTime attemptDate;
}
