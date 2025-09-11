package com.playground.gamification_manager.game.service.impl.badge.handlers;

import com.playground.gamification_manager.game.service.impl.badge.BadgesContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirstWonBadgeHandlerTest {

    @Mock
    private BadgesContext ctx;

    @InjectMocks
    private FirstWonBadgeHandler handler;

    @Test
    void conditionMet_whenFirstChallengeSolved() {
        //given
        when(ctx.getCurrentScore()).thenReturn(0);

        //when
        var result = handler.conditionMet(ctx);

        //then
        assertTrue(result);
    }

    @Test
    void conditionNotMet_whenNotFirstChallengeSolved() {
        //given
        when(ctx.getCurrentScore()).thenReturn(10);

        //when
        var result = handler.conditionMet(ctx);

        //then
        assertFalse(result);
    }
}