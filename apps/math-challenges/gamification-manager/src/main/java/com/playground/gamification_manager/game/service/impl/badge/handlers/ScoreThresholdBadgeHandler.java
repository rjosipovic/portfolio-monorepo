package com.playground.gamification_manager.game.service.impl.badge.handlers;

import com.playground.gamification_manager.game.service.impl.badge.BadgesContext;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.config.BadgesConfiguration;
import com.playground.gamification_manager.game.service.interfaces.BadgeHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class ScoreThresholdBadgeHandler implements BadgeHandler {

    private final BadgesConfiguration badgesConfiguration;

    @Override
    public boolean conditionMet(BadgesContext ctx) {
        var currentBadges = ctx.getCurrentBadges();

        if (currentBadges.contains(supports())) return false;

        var newScore = ctx.getNewScore();
        var currentScore = ctx.getCurrentScore();
        var total = newScore + currentScore;

        var badgeConfig = getBadgeConfig();
        var scoreThreshold = badgeConfig.getScoreThreshold();

        return total >= scoreThreshold;
    }

    private BadgesConfiguration.BadgeConfig getBadgeConfig() { return badgesConfiguration.getBadgeConfig(supports()); } //BadgesConfiguration.BadgeConfig
}
