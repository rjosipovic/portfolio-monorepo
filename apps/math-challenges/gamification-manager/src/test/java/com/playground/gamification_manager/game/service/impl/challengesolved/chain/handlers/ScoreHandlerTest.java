package com.playground.gamification_manager.game.service.impl.challengesolved.chain.handlers;

import com.playground.gamification_manager.game.dataaccess.repositories.ScoreRepository;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.ChallengeSolvedContext;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.config.DifficultyLevelsConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScoreHandlerTest {

    @Mock
    private DifficultyLevelsConfiguration difficultyLevelsConfiguration;

    @Mock
    private ScoreRepository scoreRepository;

    @Mock
    private ChallengeSolvedContext ctx;

    @InjectMocks
    private ScoreHandler scoreHandler;

    @BeforeEach
    void setUp() {
        lenient().when(difficultyLevelsConfiguration.getScoreMap())
                .thenReturn(Map.of(
                        "easy", 10,
                        "medium", 20,
                        "hard", 30,
                        "expert", 40)
                );
    }

    @Test
    void shouldHandle() {
        //given
        when(ctx.isCorrect()).thenReturn(true);
        //when
        var shouldHandle = scoreHandler.shouldHandle(ctx);
        //then
        assertTrue(shouldHandle);
    }

    @Test
    void shouldNotHandle() {
        //given
        when(ctx.isCorrect()).thenReturn(false);
        //when
        var shouldHandle = scoreHandler.shouldHandle(ctx);
        //then
        assertFalse(shouldHandle);
    }

    @Test
    void shouldSetScoreForEasy() {
        //given
        var userId = UUID.randomUUID();
        when(ctx.getDifficulty()).thenReturn("easy");
        when(scoreRepository.totalScoreByUserId(userId)).thenReturn(10);
        when(ctx.getUserId()).thenReturn(userId.toString());
        //when
        scoreHandler.handle(ctx);
        //then
        verify(ctx).setScore(10);
        verify(ctx).setTotalScore(10);
    }

    @Test
    void shouldSetScoreForMedium() {
        //given
        var userId = UUID.randomUUID();
        when(ctx.getDifficulty()).thenReturn("medium");
        when(scoreRepository.totalScoreByUserId(userId)).thenReturn(10);
        when(ctx.getUserId()).thenReturn(userId.toString());
        //when
        scoreHandler.handle(ctx);
        //then
        verify(ctx).setScore(20);
        verify(ctx).setTotalScore(10);
    }

    @Test
    void shouldSetScoreForHard() {
        //given
        var userId = UUID.randomUUID();
        when(ctx.getDifficulty()).thenReturn("hard");
        when(scoreRepository.totalScoreByUserId(userId)).thenReturn(10);
        when(ctx.getUserId()).thenReturn(userId.toString());
        //when
        scoreHandler.handle(ctx);
        //then
        verify(ctx).setScore(30);
        verify(ctx).setTotalScore(10);
    }

    @Test
    void shouldSetScoreForExpert() {
        //given
        var userId = UUID.randomUUID();
        when(ctx.getDifficulty()).thenReturn("expert");
        when(scoreRepository.totalScoreByUserId(userId)).thenReturn(10);
        when(ctx.getUserId()).thenReturn(userId.toString());
        //when
        scoreHandler.handle(ctx);
        //then
        verify(ctx).setScore(40);
        verify(ctx).setTotalScore(10);
    }
}