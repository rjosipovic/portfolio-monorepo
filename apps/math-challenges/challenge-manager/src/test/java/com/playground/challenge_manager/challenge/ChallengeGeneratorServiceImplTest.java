package com.playground.challenge_manager.challenge;

import com.playground.challenge_manager.challenge.services.impl.ChallengeGeneratorServiceImpl;
import com.playground.challenge_manager.challenge.services.model.Challenge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChallengeGeneratorServiceImplTest {

    @Spy
    private Random random;

    private ChallengeGeneratorServiceImpl challengeGeneratorService;

    @BeforeEach
    void setUp() {
        var digitsRangeMap = Map.of(
                "easy", Pair.of(1, 9),
                "medium", Pair.of(11, 99),
                "hard", Pair.of(101, 999),
                "expert", Pair.of(1001, 9999)
        );
        challengeGeneratorService = new ChallengeGeneratorServiceImpl(random, digitsRangeMap);
    }

    @Test
    void testGenerateChallenge_difficulty_easy() {
        var min = 1;
        var max = 9;

        when(random.nextInt((max - min) + 1)).thenReturn(1, 2);
        var challenge = challengeGeneratorService.randomChallenge("easy");
        assertNotNull(challenge);
        assertTrue(challenge.getFirstNumber() >= min && challenge.getFirstNumber() <= max);
        assertTrue(challenge.getSecondNumber() >= min && challenge.getSecondNumber() <= max);
        assertEquals(Challenge.builder().firstNumber(1 + 1).secondNumber(2 + 1).build(), challenge);
    }

    @Test
    void testGenerateChallenge_difficulty_mediume() {
        var min = 11;
        var max = 99;

        when(random.nextInt((max - min) + 1)).thenReturn(11, 12);
        var challenge = challengeGeneratorService.randomChallenge("medium");
        assertNotNull(challenge);
        assertTrue(challenge.getFirstNumber() >= min && challenge.getFirstNumber() <= max);
        assertTrue(challenge.getSecondNumber() >= min && challenge.getSecondNumber() <= max);
        assertEquals(Challenge.builder().firstNumber(11 + 11).secondNumber(12 + 11).build(), challenge);
    }

    @Test
    void testGenerateChallenge_difficulty_hard() {
        var min = 101;
        var max = 999;

        when(random.nextInt((max - min) + 1)).thenReturn(101, 102);
        var challenge = challengeGeneratorService.randomChallenge("hard");
        assertNotNull(challenge);
        assertTrue(challenge.getFirstNumber() >= min && challenge.getFirstNumber() <= max);
        assertTrue(challenge.getSecondNumber() >= min && challenge.getSecondNumber() <= max);
        assertEquals(Challenge.builder().firstNumber(101 + 101).secondNumber(102 + 101).build(), challenge);
    }

    @Test
    void testGenerateChallenge_difficulty_expert() {
        var min = 1001;
        var max = 9999;

        when(random.nextInt((max - min) + 1)).thenReturn(1001, 1002);
        var challenge = challengeGeneratorService.randomChallenge("expert");
        assertNotNull(challenge);
        assertTrue(challenge.getFirstNumber() >= min && challenge.getFirstNumber() <= max);
        assertTrue(challenge.getSecondNumber() >= min && challenge.getSecondNumber() <= max);
        assertEquals(Challenge.builder().firstNumber(1001 + 1001).secondNumber(1002 + 1001).build(), challenge);
    }
}