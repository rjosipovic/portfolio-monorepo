package com.playground.analytics_manager.outbound.api.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChallengeResult {

    String alias;
    Integer firstNumber;
    Integer secondNumber;
    Integer guess;
    Integer correctResult;
    Boolean correct;
    String game;
}
