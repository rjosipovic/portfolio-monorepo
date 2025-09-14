package com.playground.analytics_manager.inbound.messaging.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.time.ZonedDateTime;

@Getter
@ToString
public class ChallengeSolvedEvent {

    private final String userId;
    private final String challengeAttemptId;
    private final Integer firstNumber;
    private final Integer secondNumber;
    private final Integer resultAttempt;
    private final Boolean correct;
    private final String game;
    private final String difficulty;
    private final ZonedDateTime attemptDate;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public ChallengeSolvedEvent(
            @JsonProperty("userId") String userId,
            @JsonProperty("challengeAttemptId") String challengeAttemptId,
            @JsonProperty("firstNumber") int firstNumber,
            @JsonProperty("secondNumber") int secondNumber,
            @JsonProperty("resultAttempt") int resultAttempt,
            @JsonProperty("correct") boolean correct,
            @JsonProperty("game") String game,
            @JsonProperty("difficulty") String difficulty,
            @JsonProperty("attemptDate") ZonedDateTime attemptDate
    ) {
        this.userId = userId;
        this.challengeAttemptId = challengeAttemptId;
        this.firstNumber = firstNumber;
        this.secondNumber = secondNumber;
        this.resultAttempt = resultAttempt;
        this.correct = correct;
        this.game = game;
        this.difficulty = difficulty;
        this.attemptDate = attemptDate;
    }
}
