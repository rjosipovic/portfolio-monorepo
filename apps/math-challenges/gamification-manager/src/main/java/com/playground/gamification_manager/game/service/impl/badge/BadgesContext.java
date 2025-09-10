package com.playground.gamification_manager.game.service.impl.badge;

import com.playground.gamification_manager.game.dataaccess.domain.BadgeType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BadgesContext {

    Integer newScore;
    Integer currentScore;
    Set<BadgeType> currentBadges;
    Integer firstNumber;
    Integer secondNumber;
}
