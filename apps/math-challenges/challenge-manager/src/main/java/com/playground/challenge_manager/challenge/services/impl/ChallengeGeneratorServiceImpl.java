package com.playground.challenge_manager.challenge.services.impl;

import com.playground.challenge_manager.challenge.services.interfaces.ChallengeGeneratorService;
import com.playground.challenge_manager.challenge.services.model.Challenge;
import org.springframework.data.util.Pair;

import java.util.Map;
import java.util.Random;

public class ChallengeGeneratorServiceImpl implements ChallengeGeneratorService {

    private final Random random;
    private final Map<String, Pair<Integer, Integer>> digitsRangeMap;

    public ChallengeGeneratorServiceImpl(final Random random, Map<String, Pair<Integer, Integer>> digitsRangeMap) {
        this.random = random;
        this.digitsRangeMap = digitsRangeMap;
    }

    @Override
    public Challenge randomChallenge(String difficulty) {
        return Challenge.builder().firstNumber(next(difficulty)).secondNumber(next(difficulty)).build();
    }

    private int next(String difficulty) {
        var min = digitsRangeMap.get(difficulty).getFirst();
        var max = digitsRangeMap.get(difficulty).getSecond();
        return random.nextInt((max - min) + 1) + min;
    }
}
