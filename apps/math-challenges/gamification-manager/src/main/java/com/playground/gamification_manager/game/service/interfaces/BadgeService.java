package com.playground.gamification_manager.game.service.interfaces;

import com.playground.gamification_manager.game.dataaccess.domain.BadgeType;
import com.playground.gamification_manager.game.service.impl.badge.BadgesContext;

import java.util.Set;

public interface BadgeService {

    Set<BadgeType> determineBadges(BadgesContext ctx);
}
