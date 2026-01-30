package com.playground.challenge_manager.challenge.services.interfaces;

import com.playground.challenge_manager.challenge.services.model.DifficultyLevel;

import java.util.List;

public interface ChallengeGeneratorService {

    List<Integer> generate(DifficultyLevel difficultyLevel, int count);
}
