package com.playground.challenge_manager.challenge.services.interfaces;

import com.playground.challenge_manager.challenge.api.dto.ChallengeAttemptDTO;
import com.playground.challenge_manager.challenge.api.dto.ChallengeResultDTO;

import java.util.List;
import java.util.UUID;

public interface AttemptService {

    ChallengeResultDTO verifyAttempt(ChallengeAttemptDTO attempt);
    List<ChallengeResultDTO> findLast10AttemptsForUser(UUID userId);
}
