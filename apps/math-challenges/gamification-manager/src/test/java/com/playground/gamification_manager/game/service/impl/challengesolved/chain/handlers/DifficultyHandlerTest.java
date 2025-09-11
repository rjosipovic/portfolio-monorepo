package com.playground.gamification_manager.game.service.impl.challengesolved.chain.handlers;

import com.playground.gamification_manager.game.service.impl.challengesolved.chain.ChallengeSolvedContext;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.config.DifficultyLevelsConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DifficultyHandlerTest {

    @Mock
    private DifficultyLevelsConfiguration difficultyLevelsConfiguration;

    @Mock
    private ChallengeSolvedContext ctx;

    @InjectMocks
    private DifficultyHandler difficultyHandler;

    @BeforeEach
    void setUp() {
        lenient().when(difficultyLevelsConfiguration.getDifficultyMap())
                .thenReturn(Map.of(
                        1, "easy",
                        2, "medium",
                        3, "hard",
                        4, "expert")
                );
    }

    @Test
    void shouldHandle() {
        //given
        when(ctx.isCorrect()).thenReturn(true);
        //when
        var shouldHandle = difficultyHandler.shouldHandle(ctx);
        //then
        assertTrue(shouldHandle);
    }

    @Test
    void shouldNotHandle() {
        //given
        when(ctx.isCorrect()).thenReturn(false);
        //when
        var shouldHandle = difficultyHandler.shouldHandle(ctx);
        //then
        assertFalse(shouldHandle);
    }

    @Test
    void shouldSetEasyDifficulty() {
        //given
        when(ctx.getFirstNumber()).thenReturn(1);
        //when
        difficultyHandler.handle(ctx);
        //then
        verify(ctx).setDifficulty("easy");
    }

    @Test
    void shouldSetMediumDifficulty() {
        //given
        when(ctx.getFirstNumber()).thenReturn(22);
        //when
        difficultyHandler.handle(ctx);
        //then
        verify(ctx).setDifficulty("medium");
    }

    @Test
    void shouldSetHardDifficulty() {
        //given
        when(ctx.getFirstNumber()).thenReturn(333);
        //when
        difficultyHandler.handle(ctx);
        //then
        verify(ctx).setDifficulty("hard");
    }

    @Test
    void shouldSetExpertDifficulty() {
        //given
        when(ctx.getFirstNumber()).thenReturn(4444);
        //when
        difficultyHandler.handle(ctx);
        //then
        verify(ctx).setDifficulty("expert");
    }
}