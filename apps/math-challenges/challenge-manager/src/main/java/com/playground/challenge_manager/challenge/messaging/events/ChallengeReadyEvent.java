package com.playground.challenge_manager.challenge.messaging.events;

import com.playground.challenge_manager.challenge.api.dto.ChallengeResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChallengeReadyEvent {

    ChallengeResponse challenge;
}
