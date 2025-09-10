package com.playground.gamification_manager.game.service.interfaces;

import com.playground.gamification_manager.game.dataaccess.domain.BadgeType;
import com.playground.gamification_manager.game.service.impl.badge.BadgesContext;

public interface BadgeHandler {

    boolean conditionMet(BadgesContext ctx);
    BadgeType supports();
}
