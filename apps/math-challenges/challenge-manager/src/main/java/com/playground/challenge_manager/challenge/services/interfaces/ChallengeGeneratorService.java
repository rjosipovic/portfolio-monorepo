package com.playground.challenge_manager.challenge.services.interfaces;

import com.playground.challenge_manager.challenge.services.model.Challenge;

public interface ChallengeGeneratorService {

    Challenge randomChallenge(String difficulty);
}
