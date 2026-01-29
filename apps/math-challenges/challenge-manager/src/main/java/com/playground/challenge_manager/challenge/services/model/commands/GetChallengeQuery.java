package com.playground.challenge_manager.challenge.services.model.commands;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GetChallengeQuery {

    @NonNull
    UUID challengeId;
    @NonNull
    UUID userId;
}
