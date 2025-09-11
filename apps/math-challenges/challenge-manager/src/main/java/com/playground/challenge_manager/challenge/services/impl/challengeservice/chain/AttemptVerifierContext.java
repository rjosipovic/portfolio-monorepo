package com.playground.challenge_manager.challenge.services.impl.challengeservice.chain;

import com.playground.challenge_manager.challenge.api.dto.ChallengeAttemptDTO;
import com.playground.challenge_manager.challenge.api.dto.ChallengeResultDTO;
import com.playground.challenge_manager.challenge.services.model.ChallengeAttempt;
import lombok.Getter;
import lombok.Setter;

@Getter
public class AttemptVerifierContext {

    private final ChallengeAttemptDTO attempt;
    @Setter
    private ChallengeAttempt challengeAttempt;
    @Setter
    private ChallengeResultDTO challengeResult;

    public AttemptVerifierContext(final ChallengeAttemptDTO attempt) {
        this.attempt = attempt;
    }
}
