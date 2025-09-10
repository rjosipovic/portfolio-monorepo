package com.playground.challenge_manager.challenge.api.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChallengeResultDTO {

    String userId;
    int firstNumber;
    int secondNumber;
    int guess;
    int correctResult;
    boolean correct;
    String game;
}
