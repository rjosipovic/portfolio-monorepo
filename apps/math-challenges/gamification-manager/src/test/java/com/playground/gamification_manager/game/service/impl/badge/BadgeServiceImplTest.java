package com.playground.gamification_manager.game.service.impl.badge;

import com.playground.gamification_manager.game.dataaccess.domain.BadgeType;
import com.playground.gamification_manager.game.service.interfaces.BadgeHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BadgeServiceImplTest {

    @Mock
    private BadgesContext ctx;
    @Mock
    private BadgeHandler firstWonBadgeHandler;
    @Mock
    private BadgeHandler luckyNumberBadgeHandler;
    @Mock
    private BadgeHandler bronzeBadgeHandler;
    @Mock
    private BadgeHandler silverBadgeHandler;
    @Mock
    private BadgeHandler goldBadgeHandler;

    private BadgeServiceImpl badgeService;

    @BeforeEach
    void init() {
        var badgeHandlers = List.of(firstWonBadgeHandler, luckyNumberBadgeHandler, bronzeBadgeHandler, silverBadgeHandler, goldBadgeHandler);
        badgeService = new BadgeServiceImpl(badgeHandlers);
    }

    @Test
    void shouldDetermineAllBadges() {
        //given
        when(firstWonBadgeHandler.supports()).thenReturn(BadgeType.FIRST_WON);
        when(luckyNumberBadgeHandler.supports()).thenReturn(BadgeType.LUCKY_NUMBER);
        when(bronzeBadgeHandler.supports()).thenReturn(BadgeType.BRONZE);
        when(silverBadgeHandler.supports()).thenReturn(BadgeType.SILVER);
        when(goldBadgeHandler.supports()).thenReturn(BadgeType.GOLD);

        when(firstWonBadgeHandler.conditionMet(ctx)).thenReturn(true);
        when(luckyNumberBadgeHandler.conditionMet(ctx)).thenReturn(true);
        when(bronzeBadgeHandler.conditionMet(ctx)).thenReturn(true);
        when(silverBadgeHandler.conditionMet(ctx)).thenReturn(true);
        when(goldBadgeHandler.conditionMet(ctx)).thenReturn(true);
        //when
        var resultBadges = badgeService.determineBadges(ctx);

        //then
        assertAll(
                () -> assertFalse(resultBadges.isEmpty()),
                () -> assertEquals(5, resultBadges.size()),
                () -> assertTrue(resultBadges.contains(BadgeType.FIRST_WON)),
                () -> assertTrue(resultBadges.contains(BadgeType.LUCKY_NUMBER)),
                () -> assertTrue(resultBadges.contains(BadgeType.BRONZE)),
                () -> assertTrue(resultBadges.contains(BadgeType.SILVER)),
                () -> assertTrue(resultBadges.contains(BadgeType.GOLD))
        );
    }

    @Test
    void shouldDetermineSomeBadges() {
        //given
        when(firstWonBadgeHandler.supports()).thenReturn(BadgeType.FIRST_WON);
        when(luckyNumberBadgeHandler.supports()).thenReturn(BadgeType.LUCKY_NUMBER);

        when(firstWonBadgeHandler.conditionMet(ctx)).thenReturn(true);
        when(luckyNumberBadgeHandler.conditionMet(ctx)).thenReturn(true);
        when(bronzeBadgeHandler.conditionMet(ctx)).thenReturn(false);
        when(silverBadgeHandler.conditionMet(ctx)).thenReturn(false);
        when(goldBadgeHandler.conditionMet(ctx)).thenReturn(false);
        //when
        var resultBadges = badgeService.determineBadges(ctx);

        //then
        assertAll(
                () -> assertFalse(resultBadges.isEmpty()),
                () -> assertEquals(2, resultBadges.size()),
                () -> assertTrue(resultBadges.contains(BadgeType.FIRST_WON)),
                () -> assertTrue(resultBadges.contains(BadgeType.LUCKY_NUMBER)),
                () -> assertFalse(resultBadges.contains(BadgeType.BRONZE)),
                () -> assertFalse(resultBadges.contains(BadgeType.SILVER)),
                () -> assertFalse(resultBadges.contains(BadgeType.GOLD))
        );
    }
}