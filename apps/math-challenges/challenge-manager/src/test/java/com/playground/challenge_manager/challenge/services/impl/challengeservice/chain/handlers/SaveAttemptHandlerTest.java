package com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.handlers;

import com.playground.challenge_manager.challenge.api.dto.ChallengeAttemptDTO;
import com.playground.challenge_manager.challenge.dataaccess.entities.ChallengeAttemptEntity;
import com.playground.challenge_manager.challenge.dataaccess.repositories.ChallengeAttemptRepository;
import com.playground.challenge_manager.challenge.mappers.ChallengeMapper;
import com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.AttemptVerifierContext;
import com.playground.challenge_manager.challenge.services.model.ChallengeAttempt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaveAttemptHandlerTest {

    @Mock
    private ChallengeAttemptRepository challengeAttemptRepository;
    private final ChallengeMapper challengeMapper = Mappers.getMapper(ChallengeMapper.class);

    private SaveAttemptHandler saveAttemptHandler;

    @BeforeEach
    void init() {
        saveAttemptHandler = new SaveAttemptHandler(challengeAttemptRepository, challengeMapper);
    }

    @Test
    void shouldSaveAttempt() {
        //given
        var userId = UUID.randomUUID();
        var challengeAttemptId = UUID.randomUUID();
        var firstNumber = 12;
        var secondNumber = 23;
        var guess = 2764;
        var game = "multiplication";
        var difficulty = "easy";
        var attempt = ChallengeAttemptDTO.builder()
                .userId(userId.toString())
                .firstNumber(firstNumber)
                .secondNumber(secondNumber)
                .guess(guess)
                .game(game)
                .build();
        var ctx = new AttemptVerifierContext(attempt);
        var challengeAttempt = ChallengeAttempt.builder()
                .userId(userId)
                .firstNumber(firstNumber)
                .secondNumber(secondNumber)
                .resultAttempt(guess)
                .correct(false)
                .game(game)
                .difficulty(difficulty)
                .build();
        ctx.setChallengeAttempt(challengeAttempt);

        var saved = mock(ChallengeAttemptEntity.class);
        when(saved.getId()).thenReturn(challengeAttemptId);
        when(saved.getAttemptDate()).thenReturn(ZonedDateTime.now());
        when(challengeAttemptRepository.saveAndFlush(any(ChallengeAttemptEntity.class))).thenReturn(saved);

        //when
        saveAttemptHandler.handle(ctx);
        //then
        verify(challengeAttemptRepository, times(1)).saveAndFlush(any(ChallengeAttemptEntity.class));
    }
}