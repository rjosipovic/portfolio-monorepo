package com.playground.gamification_manager.game.service.impl.badge.handlers;

import com.playground.gamification_manager.game.dataaccess.domain.BadgeType;
import com.playground.gamification_manager.game.service.impl.badge.BadgesContext;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.config.BadgesConfiguration;
import com.playground.gamification_manager.game.service.interfaces.BadgeHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LuckyNumberBadgeHandler implements BadgeHandler {

    private final BadgesConfiguration badgesConfiguration;

    @Override
    public boolean conditionMet(BadgesContext ctx) {
        var currentBadges = ctx.getCurrentBadges();
        if (currentBadges.contains(supports())) return false;

        var firstNumber = ctx.getFirstNumber();
        var secondNumber = ctx.getSecondNumber();

        var badgeConfig = getBadgeConfig();
        var luckyNumber = badgeConfig.getLuckyNumber();
        return firstNumber.equals(luckyNumber) || secondNumber.equals(luckyNumber);
    }

    private BadgesConfiguration.BadgeConfig getBadgeConfig() {
        return badgesConfiguration.getBadgeConfig(supports());
    }

    @Override
    public BadgeType supports() {
        return BadgeType.LUCKY_NUMBER;
    }
}
