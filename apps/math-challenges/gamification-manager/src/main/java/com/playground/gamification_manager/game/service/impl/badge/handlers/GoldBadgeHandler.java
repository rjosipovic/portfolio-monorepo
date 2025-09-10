package com.playground.gamification_manager.game.service.impl.badge.handlers;

import com.playground.gamification_manager.game.dataaccess.domain.BadgeType;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.config.BadgesConfiguration;
import org.springframework.stereotype.Service;

@Service
public class GoldBadgeHandler extends ScoreThresholdBadgeHandler {

    public GoldBadgeHandler(BadgesConfiguration badgesConfiguration) {
        super(badgesConfiguration);
    }

    @Override
    public BadgeType supports() {
        return BadgeType.GOLD;
    }
}
