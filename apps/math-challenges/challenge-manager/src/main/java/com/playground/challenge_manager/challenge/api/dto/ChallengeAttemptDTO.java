package com.playground.challenge_manager.challenge.api.dto;

import com.playground.challenge_manager.challenge.api.validation.SameDigitCount;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SameDigitCount(message = "Numbers must have the same digit count")
public class ChallengeAttemptDTO {

    @UUID
    @With
    String userId;
    @NotNull
    @Range(min = 1, max = 9999)
    Integer firstNumber;
    @NotNull
    @Range(min = 1, max = 9999)
    Integer secondNumber;
    @NotNull
    Integer guess;
    @NotNull @Pattern(regexp = "addition|subtraction|multiplication|division")
    String game;
}
