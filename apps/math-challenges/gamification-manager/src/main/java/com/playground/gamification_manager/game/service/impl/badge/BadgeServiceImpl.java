package com.playground.gamification_manager.game.service.impl.badge;

import com.playground.gamification_manager.game.dataaccess.domain.BadgeType;
import com.playground.gamification_manager.game.service.interfaces.BadgeHandler;
import com.playground.gamification_manager.game.service.interfaces.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BadgeServiceImpl implements BadgeService {

    private final List<BadgeHandler> badgeHandlers;

    @Override
    public Set<BadgeType> determineBadges(BadgesContext ctx) {

        return badgeHandlers.stream()
                .filter(badgeHandler -> badgeHandler.conditionMet(ctx))
                .map(BadgeHandler::supports)
                .collect(Collectors.toSet());
    }
}
