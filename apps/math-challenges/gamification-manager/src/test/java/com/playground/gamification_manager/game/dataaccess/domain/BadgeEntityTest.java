package com.playground.gamification_manager.game.dataaccess.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class BadgeEntityTest {

    @Test
    void whenCreateBadgeWithNullUserId_thenThrowException() {
        // given
        var badgeType = BadgeType.BRONZE;

        // when & then
        assertThrows(IllegalArgumentException.class, () -> BadgeEntity.create(null, badgeType));
    }

    @Test
    void whenCreateBadgeWithNullBadgeType_thenThrowException() {
        // given
        var userId = UUID.randomUUID();

        // when & then
        assertThrows(IllegalArgumentException.class, () -> BadgeEntity.create(userId, null));
    }
}
