package com.playground.challenge_manager.challenge.services.model.commands;

import com.playground.challenge_manager.challenge.services.model.DifficultyLevel;
import com.playground.challenge_manager.challenge.services.model.OperationType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChallengeCreationCommand {

    @NonNull
    DifficultyLevel difficulty;
    @NonNull
    OperationType operation;
    @NonNull
    Integer operandCount;
    @NonNull
    UUID userId;
}
