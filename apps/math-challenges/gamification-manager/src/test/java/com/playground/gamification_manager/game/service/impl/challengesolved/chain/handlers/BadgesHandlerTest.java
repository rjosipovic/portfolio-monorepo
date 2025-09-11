package com.playground.gamification_manager.game.service.impl.challengesolved.chain.handlers;

import com.playground.gamification_manager.game.dataaccess.domain.BadgeEntity;
import com.playground.gamification_manager.game.dataaccess.domain.BadgeType;
import com.playground.gamification_manager.game.dataaccess.repositories.BadgeRepository;
import com.playground.gamification_manager.game.service.impl.badge.BadgesContext;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.ChallengeSolvedContext;
import com.playground.gamification_manager.game.service.interfaces.BadgeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BadgesHandlerTest {

    @Mock
    private BadgeRepository badgeRepository;
    @Mock
    private BadgeService badgeService;
    @Mock
    private ChallengeSolvedContext ctx;

    @InjectMocks
    private BadgesHandler badgesHandler;

    @Test
    void shouldHandle() {
        //given
        when(ctx.isCorrect()).thenReturn(true);
        //when
        var result = badgesHandler.shouldHandle(ctx);
        //then
        assertTrue(result);
    }

    @Test
    void shouldNotHandle() {
        //given
        when(ctx.isCorrect()).thenReturn(false);
        //when
        var result = badgesHandler.shouldHandle(ctx);
        //then
        assertFalse(result);
    }

    @Test
    void shouldDetermineNewBadges() {
        //given
        var userId = UUID.randomUUID();
        when(ctx.getUserId()).thenReturn(userId.toString());
        var score = 10;
        var firstNumber = 11;
        var secondNumber = 12;
        var currentScore = 90;
        List<BadgeEntity> currentBadges = List.of();
        var badgesCtx = BadgesContext.builder()
                .newScore(score)
                .currentScore(currentScore)
                .currentBadges(Set.of())
                .firstNumber(firstNumber)
                .secondNumber(secondNumber)
                .build();
        when(badgeRepository.findAllByUserId(userId)).thenReturn(currentBadges);
        when(ctx.getScore()).thenReturn(score);
        when(ctx.getFirstNumber()).thenReturn(firstNumber);
        when(ctx.getSecondNumber()).thenReturn(secondNumber);
        when(ctx.getTotalScore()).thenReturn(currentScore);
        when(badgeService.determineBadges(badgesCtx)).thenReturn(Set.of(BadgeType.BRONZE));
        //when
        badgesHandler.handle(ctx);
        //then
        verify(ctx).addBadges(Set.of(BadgeType.BRONZE));
    }
}