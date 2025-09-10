package com.playground.gamification_manager.game.service.impl.badge.handlers;

import com.playground.gamification_manager.game.dataaccess.domain.BadgeType;
import com.playground.gamification_manager.game.service.impl.badge.BadgesContext;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.config.BadgesConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SilverBadgeHandlerTest {

    @Mock
    private BadgesContext ctx;

    @Mock
    private BadgesConfiguration badgesConfiguration;

    @InjectMocks
    private SilverBadgeHandler handler;

    @Test
    void conditionNotMet_whenThresholdReachedAndBadgeAlreadyAwarded() {
        //given
        when(ctx.getCurrentBadges()).thenReturn(Set.of(BadgeType.SILVER));

        //when
        var result  = handler.conditionMet(ctx);

        //then
        assertFalse(result);
    }

    @Test
    void conditionMet_whenThresholdReached() {
        //given
        when(badgesConfiguration.getBadgeConfig(BadgeType.SILVER)).thenReturn(new BadgesConfiguration.BadgeConfig(BadgeType.SILVER, 250, null));
        when(ctx.getNewScore()).thenReturn(10);
        when(ctx.getCurrentScore()).thenReturn(240);

        //when
        var result  = handler.conditionMet(ctx);

        //then
        assertTrue(result);
    }

    @Test
    void conditionMet_whenThresholdNotReached() {
        //given
        when(badgesConfiguration.getBadgeConfig(BadgeType.SILVER)).thenReturn(new BadgesConfiguration.BadgeConfig(BadgeType.SILVER, 250, null));
        when(ctx.getNewScore()).thenReturn(10);
        when(ctx.getCurrentScore()).thenReturn(220);

        //when
        var result  = handler.conditionMet(ctx);

        //then
        assertFalse(result);
    }
}