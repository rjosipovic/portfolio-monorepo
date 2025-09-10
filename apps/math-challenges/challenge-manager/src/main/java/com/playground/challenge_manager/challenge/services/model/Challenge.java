package com.playground.challenge_manager.challenge.services.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Challenge {

    Integer firstNumber;
    Integer secondNumber;
}
