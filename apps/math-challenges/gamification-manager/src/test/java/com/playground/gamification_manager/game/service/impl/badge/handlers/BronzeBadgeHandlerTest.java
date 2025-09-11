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
class BronzeBadgeHandlerTest {

    @Mock
    private BadgesContext ctx;

    @Mock
    private BadgesConfiguration badgesConfiguration;

    @InjectMocks
    private BronzeBadgeHandler handler;

    @Test
    void conditionNotMet_whenThresholdReachedAndBadgeAlreadyAwarded() {
        //given
        when(ctx.getCurrentBadges()).thenReturn(Set.of(BadgeType.BRONZE));

        //when
        var result  = handler.conditionMet(ctx);

        //then
        assertFalse(result);
    }

    @Test
    void conditionMet_whenThresholdReached() {
        //given
        when(badgesConfiguration.getBadgeConfig(BadgeType.BRONZE)).thenReturn(new BadgesConfiguration.BadgeConfig(BadgeType.BRONZE, 100, null));
        when(ctx.getNewScore()).thenReturn(10);
        when(ctx.getCurrentScore()).thenReturn(90);

        //when
        var result  = handler.conditionMet(ctx);

        //then
        assertTrue(result);
    }

    @Test
    void conditionMet_whenThresholdNotReached() {
        //given
        when(badgesConfiguration.getBadgeConfig(BadgeType.BRONZE)).thenReturn(new BadgesConfiguration.BadgeConfig(BadgeType.BRONZE, 100, null));
        when(ctx.getNewScore()).thenReturn(10);
        when(ctx.getCurrentScore()).thenReturn(80);

        //when
        var result  = handler.conditionMet(ctx);

        //then
        assertFalse(result);
    }
}