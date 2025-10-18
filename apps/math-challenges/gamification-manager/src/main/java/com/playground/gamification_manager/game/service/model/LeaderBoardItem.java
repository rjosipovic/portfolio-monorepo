package com.playground.gamification_manager.game.service.model;

import com.playground.gamification_manager.game.dataaccess.domain.BadgeType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.Set;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LeaderBoardItem {

    String alias;
    long totalScore;
    @Singular
    Set<BadgeType> badges;
}
