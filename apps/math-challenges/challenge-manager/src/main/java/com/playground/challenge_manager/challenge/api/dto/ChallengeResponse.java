package com.playground.challenge_manager.challenge.api.dto;

import com.playground.challenge_manager.challenge.services.model.ChallengeStatus;
import com.playground.challenge_manager.challenge.services.model.DifficultyLevel;
import com.playground.challenge_manager.challenge.services.model.OperationType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChallengeResponse {

    UUID id;
    ChallengeStatus status;
    List<Integer> operands;
    OperationType operation;
    DifficultyLevel difficulty;
    ZonedDateTime expiresAt;
}
