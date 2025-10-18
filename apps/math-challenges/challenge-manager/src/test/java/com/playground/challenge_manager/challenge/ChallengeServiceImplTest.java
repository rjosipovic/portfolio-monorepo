package com.playground.challenge_manager.challenge;

import com.playground.challenge_manager.challenge.api.dto.ChallengeAttemptDTO;
import com.playground.challenge_manager.challenge.dataaccess.entities.ChallengeAttemptEntity;
import com.playground.challenge_manager.challenge.dataaccess.repositories.ChallengeAttemptRepository;
import com.playground.challenge_manager.challenge.mappers.ChallengeMapper;
import com.playground.challenge_manager.challenge.services.config.ChallengeConfig;
import com.playground.challenge_manager.challenge.services.impl.challengeservice.ChallengeServiceImpl;
import com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.AttemptVerifierChain;
import com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.handlers.AttemptResultHandler;
import com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.handlers.CheckResultHandler;
import com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.handlers.SaveAttemptHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChallengeServiceImplTest {

    @Mock
    private ChallengeConfig challengeConfig;
    @Mock
    private ChallengeAttemptRepository challengeAttemptRepository;
    private ChallengeServiceImpl challengeService;
    private final ChallengeMapper challengeMapper = Mappers.getMapper(ChallengeMapper.class);

    @BeforeEach
    void setUp() {
        var checkResultHandler = new CheckResultHandler(challengeConfig, challengeMapper);
        var saveAttemptHandler = new SaveAttemptHandler(challengeAttemptRepository, challengeMapper);
        var chain = new AttemptVerifierChain();
        chain.addHandler(checkResultHandler);
        chain.addHandler(saveAttemptHandler);
        chain.addHandler(new AttemptResultHandler(challengeMapper));
        challengeService = new ChallengeServiceImpl(chain, challengeAttemptRepository, challengeMapper);
    }

    @Test
    void testVerifyCorrectAttempt() {
        //given
        var userId = UUID.randomUUID();
        var challengeAttemptId = UUID.randomUUID();
        var firstNumber = 12;
        var secondNumber = 23;
        var guess = firstNumber * secondNumber;
        var game = "multiplication";
        var attemptDto = ChallengeAttemptDTO.builder()
                .userId(userId.toString())
                .firstNumber(firstNumber)
                .secondNumber(secondNumber)
                .guess(guess)
                .game(game)
                .build();
        var savedAttemptEntity = mock(ChallengeAttemptEntity.class);
        when(savedAttemptEntity.getId()).thenReturn(challengeAttemptId);
        when(savedAttemptEntity.getAttemptDate()).thenReturn(ZonedDateTime.now());
        when(challengeAttemptRepository.saveAndFlush(any(ChallengeAttemptEntity.class))).thenReturn(savedAttemptEntity);
        when(challengeConfig.getDifficultyLevels()).thenReturn(
                List.of(
                        new ChallengeConfig.DifficultyLevel("easy", 1, 9),
                        new ChallengeConfig.DifficultyLevel("medium", 10, 99),
                        new ChallengeConfig.DifficultyLevel("hard", 100, 999),
                        new ChallengeConfig.DifficultyLevel("expert", 1000, 9999)
                )
        );

        //when
        var result = challengeService.verifyAttempt(attemptDto);

        //then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(firstNumber, result.getFirstNumber()),
                () -> assertEquals(secondNumber, result.getSecondNumber()),
                () -> assertEquals(guess, result.getGuess()),
                () -> assertEquals(guess, result.getCorrectResult()),
                () -> assertTrue(result.isCorrect())
        );

        verify(challengeAttemptRepository).saveAndFlush(any(ChallengeAttemptEntity.class));
    }

    @Test
    void testVerifyIncorrectAttempt() {
        //given
        var userId = UUID.randomUUID();
        var firstNumber = 12;
        var secondNumber = 23;
        var guess = firstNumber * secondNumber + 1;
        var correctResult = firstNumber * secondNumber;
        var game = "multiplication";
        var attempt = ChallengeAttemptDTO.builder()
                .userId(userId.toString())
                .firstNumber(firstNumber)
                .secondNumber(secondNumber)
                .guess(guess)
                .game(game)
                .build();
        var savedAttemptEntity = mock(ChallengeAttemptEntity.class);
        when(challengeAttemptRepository.saveAndFlush(any(ChallengeAttemptEntity.class))).thenReturn(savedAttemptEntity);
        when(challengeConfig.getDifficultyLevels()).thenReturn(
                List.of(
                        new ChallengeConfig.DifficultyLevel("easy", 1, 9),
                        new ChallengeConfig.DifficultyLevel("medium", 10, 99),
                        new ChallengeConfig.DifficultyLevel("hard", 100, 999),
                        new ChallengeConfig.DifficultyLevel("expert", 1000, 9999)
                )
        );

        //when
        var result = challengeService.verifyAttempt(attempt);

        //then
        assertAll(
                () -> assertNotNull(attempt),
                () -> assertEquals(firstNumber, attempt.getFirstNumber()),
                () -> assertEquals(secondNumber, attempt.getSecondNumber()),
                () -> assertEquals(guess, attempt.getGuess()),
                () -> assertEquals(correctResult, result.getCorrectResult()),
                () -> assertFalse(result.isCorrect())
        );

        verify(challengeAttemptRepository).saveAndFlush(any(ChallengeAttemptEntity.class));
    }

    @Test
    void testGetLast10AttemptsByUserId_shouldReturnEmptyList() {
        //given
        var userId = UUID.randomUUID();
        when(challengeAttemptRepository.findLast10AttemptsByUser(userId)).thenReturn(List.of());
        //when
        var result = challengeService.findLast10AttemptsForUser(userId);
        //then
        verify(challengeAttemptRepository).findLast10AttemptsByUser(userId);
        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty())
        );
    }

    @Test
    void testGetLast10AttemptsByUserId_shouldReturnNonEmptyList() {
        //given
        var userId = UUID.randomUUID();
        var game = "multiplication";
        var attempt = mock(ChallengeAttemptEntity.class);
        when(attempt.getUserId()).thenReturn(userId);
        when(attempt.getFirstNumber()).thenReturn(1);
        when(attempt.getSecondNumber()).thenReturn(2);
        when(attempt.getResultAttempt()).thenReturn(3);
        when(attempt.getCorrect()).thenReturn(true);
        when(attempt.getGame()).thenReturn(game);
        when(challengeAttemptRepository.findLast10AttemptsByUser(userId)).thenReturn(List.of(attempt));
        //when
        var result = challengeService.findLast10AttemptsForUser(userId);
        //then
        verify(challengeAttemptRepository).findLast10AttemptsByUser(userId);
        assertAll(
                () -> assertNotNull(result),
                () -> assertFalse(result.isEmpty()),
                () -> assertEquals(1, result.size()),
                () -> assertEquals(userId.toString(), result.get(0).getUserId()),
                () -> assertEquals(1, result.get(0).getFirstNumber()),
                () -> assertEquals(2, result.get(0).getSecondNumber()),
                () -> assertEquals(3, result.get(0).getGuess()),
                () -> assertTrue(result.get(0).isCorrect())
        );
    }
}