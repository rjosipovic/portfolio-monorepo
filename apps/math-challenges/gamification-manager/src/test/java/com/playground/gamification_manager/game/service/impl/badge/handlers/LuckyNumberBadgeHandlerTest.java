package com.playground.gamification_manager.game.service.impl.badge.handlers;

import com.playground.gamification_manager.game.dataaccess.domain.BadgeType;
import com.playground.gamification_manager.game.service.impl.badge.BadgesContext;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.config.BadgesConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LuckyNumberBadgeHandlerTest {

    @Mock
    private BadgesContext ctx;

    @Mock
    private BadgesConfiguration badgesConfiguration;

    @InjectMocks
    private LuckyNumberBadgeHandler handler;

    @Test
    void conditionMet_whenFirstNumberIsLucky() {
        //given
        when(ctx.getFirstNumber()).thenReturn(12);
        when(ctx.getSecondNumber()).thenReturn(42);
        when(badgesConfiguration.getBadgeConfig(BadgeType.LUCKY_NUMBER)).thenReturn(new BadgesConfiguration.BadgeConfig(BadgeType.LUCKY_NUMBER,null, 42));

        //when
        var result = handler.conditionMet(ctx);

        //then
        assertTrue(result);
    }

    @Test
    void conditionMet_whenSecondNumberIsLucky() {
        //given
        when(ctx.getFirstNumber()).thenReturn(42);
        when(ctx.getSecondNumber()).thenReturn(12);
        when(badgesConfiguration.getBadgeConfig(BadgeType.LUCKY_NUMBER)).thenReturn(new BadgesConfiguration.BadgeConfig(BadgeType.LUCKY_NUMBER,null, 42));

        //when
        var result = handler.conditionMet(ctx);

        //then
        assertTrue(result);
    }

    @Test
    void conditionNotMet_whenNumbersAreNotLucky() {
        //given
        when(ctx.getFirstNumber()).thenReturn(15);
        when(ctx.getSecondNumber()).thenReturn(12);
        when(badgesConfiguration.getBadgeConfig(BadgeType.LUCKY_NUMBER)).thenReturn(new BadgesConfiguration.BadgeConfig(BadgeType.LUCKY_NUMBER,null, 42));

        //when
        var result = handler.conditionMet(ctx);

        //then
        assertFalse(result);
    }
}