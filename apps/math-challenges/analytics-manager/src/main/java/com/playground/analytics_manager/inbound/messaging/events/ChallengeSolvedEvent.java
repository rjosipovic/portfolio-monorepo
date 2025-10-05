package com.playground.analytics_manager.inbound.messaging.events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.ZonedDateTime;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = ChallengeSolvedEvent.ChallengeSolvedEventBuilder.class)
public class ChallengeSolvedEvent {

    String userId;
    String challengeAttemptId;
    Integer firstNumber;
    Integer secondNumber;
    Integer resultAttempt;
    Boolean correct;
    String game;
    String difficulty;
    ZonedDateTime attemptDate;

    @JsonPOJOBuilder(withPrefix = "", buildMethodName = "build")
    public static class ChallengeSolvedEventBuilder {}

}
