package com.playground.challenge_manager.challenge.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class AttemptRequest {

    @NotNull(message = "Guess is required")
    Integer guess;
}
