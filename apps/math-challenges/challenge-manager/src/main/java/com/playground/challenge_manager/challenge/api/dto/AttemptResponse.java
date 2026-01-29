package com.playground.challenge_manager.challenge.api.dto;

import com.playground.challenge_manager.challenge.services.model.ChallengeStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AttemptResponse {

    UUID challengeId;
    Boolean correct;
    ChallengeStatus status;
}
