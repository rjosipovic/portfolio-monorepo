package com.playground.gamification_manager.game.service.impl.challengesolved.chain.handlers;

import com.playground.gamification_manager.game.dataaccess.domain.ScoreEntity;
import com.playground.gamification_manager.game.dataaccess.repositories.ScoreRepository;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.ChallengeSolvedContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaveScoreHandlerTest {

    @Mock
    private ScoreRepository scoreRepository;

    @Mock
    private ChallengeSolvedContext ctx;

    @InjectMocks
    private SaveScoreHandler saveScoreHandler;

    @Test
    void shouldHandle() {
        //given
        when(ctx.getScore()).thenReturn(20);
        //when
        var shouldHandle = saveScoreHandler.shouldHandle(ctx);
        //then
        assertTrue(shouldHandle);
    }

    @Test
    void shouldNotHandle() {
        //given
        when(ctx.getScore()).thenReturn(0);
        //when
        var shouldHandle = saveScoreHandler.shouldHandle(ctx);
        //then
        assertFalse(shouldHandle);
    }

    @Test
    void shouldSaveScore() {
        //given
        var userId = UUID.randomUUID().toString();
        var challengeAttemptId = UUID.randomUUID().toString();
        when(ctx.getUserId()).thenReturn(userId);
        when(ctx.getScore()).thenReturn(20);
        when(ctx.getChallengeAttemptId()).thenReturn(challengeAttemptId);

        //when
        saveScoreHandler.handle(ctx);
        //then
        ArgumentCaptor<ScoreEntity> captor = ArgumentCaptor.forClass(ScoreEntity.class);
        verify(scoreRepository).save(captor.capture());

        assertAll(
                () -> assertEquals(userId, captor.getValue().getUserId().toString()),
                () -> assertEquals(challengeAttemptId, captor.getValue().getChallengeAttemptId().toString()),
                () -> assertEquals(20, captor.getValue().getScore())
        );
    }
}