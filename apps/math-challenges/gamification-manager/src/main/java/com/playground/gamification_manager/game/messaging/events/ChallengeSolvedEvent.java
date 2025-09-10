package com.playground.gamification_manager.game.messaging.events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = ChallengeSolvedEvent.ChallengeSolvedEventBuilder.class)
public class ChallengeSolvedEvent {

    @UUID @NotNull
    String userId;

    @UUID @NotNull
    String challengeAttemptId;

    @NotNull @Range(min = 1, max = 9999)
    Integer firstNumber;

    @NotNull @Range(min = 1, max = 9999)
    Integer secondNumber;

    @NotNull
    Boolean correct;

    @NotNull @Pattern(regexp = "addition|subtraction|multiplication|division")
    String game;

    @JsonPOJOBuilder(withPrefix = "", buildMethodName = "build")
    public static class ChallengeSolvedEventBuilder {
    }
}
