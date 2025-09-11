package com.playground.gamification_manager.game.service.impl.challengesolved.chain.handlers;

import com.playground.gamification_manager.game.dataaccess.domain.BadgeEntity;
import com.playground.gamification_manager.game.dataaccess.domain.BadgeType;
import com.playground.gamification_manager.game.dataaccess.repositories.BadgeRepository;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.ChallengeSolvedContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaveBadgesHandlerTest {

    @Mock
    private BadgeRepository badgeRepository;

    @Mock
    private ChallengeSolvedContext ctx;

    @InjectMocks
    private SaveBadgesHandler saveBadgesHandler;

    @Test
    void shouldHandle() {
        //given
        when(ctx.getBadges()).thenReturn(Set.of(BadgeType.FIRST_WON));
        //when
        saveBadgesHandler.shouldHandle(ctx);
        //then
        assertTrue(saveBadgesHandler.shouldHandle(ctx));
    }

    @Test
    void shouldNotHandle() {
        //given
        when(ctx.getBadges()).thenReturn(Set.of());
        //when
        saveBadgesHandler.shouldHandle(ctx);
        //then
        assertFalse(saveBadgesHandler.shouldHandle(ctx));
    }

    @Test
    void shouldSaveBadges() {
        //given
        var userId = UUID.randomUUID().toString();
        when(ctx.getUserId()).thenReturn(userId);
        when(ctx.getBadges()).thenReturn(Set.of(BadgeType.FIRST_WON));
        var badgeEntity = new BadgeEntity();
        badgeEntity.setUserId(UUID.fromString(userId));
        badgeEntity.setBadgeType(BadgeType.FIRST_WON);
        //when
        saveBadgesHandler.handle(ctx);
        //then
        ArgumentCaptor<List<BadgeEntity>> capture = ArgumentCaptor.forClass(List.class);
        verify(badgeRepository).saveAll(capture.capture());

        List<BadgeEntity> savedBadges = capture.getValue();
        assertAll(
                () -> assertEquals(1, savedBadges.size()),
                () -> assertEquals(userId, savedBadges.get(0).getUserId().toString()),
                () -> assertEquals(BadgeType.FIRST_WON, savedBadges.get(0).getBadgeType())
        );
    }
}