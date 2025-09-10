package com.playground.gamification_manager.game.service.impl.badge.handlers;

import com.playground.gamification_manager.game.dataaccess.domain.BadgeType;
import com.playground.gamification_manager.game.service.impl.badge.BadgesContext;
import com.playground.gamification_manager.game.service.interfaces.BadgeHandler;
import org.springframework.stereotype.Service;

@Service
public class FirstWonBadgeHandler implements BadgeHandler {

    @Override
    public boolean conditionMet(BadgesContext ctx) {
        var currentBadges = ctx.getCurrentBadges();
        if (currentBadges.contains(supports())) return false;

        var currentScore = ctx.getCurrentScore();
        return currentScore == 0;
    }

    @Override
    public BadgeType supports() {
        return BadgeType.FIRST_WON;
    }
}
