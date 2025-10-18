package com.playground.gamification_manager.game.service.impl.challengesolved.chain.handlers;

import com.playground.gamification_manager.game.dataaccess.domain.BadgeEntity;
import com.playground.gamification_manager.game.dataaccess.repositories.BadgeRepository;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.ChallengeSolvedContext;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.ChallengeSolvedHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaveBadgesHandler implements ChallengeSolvedHandler {

    private final BadgeRepository badgeRepository;

    @Override
    public boolean shouldHandle(ChallengeSolvedContext ctx) {
        return hasBadges(ctx);
    }

    @Override
    public void handle(ChallengeSolvedContext ctx) {
        saveBadges(ctx);
    }

    private void saveBadges(ChallengeSolvedContext ctx) {
        var userId = ctx.getUserId();
        var badges = ctx.getBadges();
        var badgeEntities = badges.stream().map(badge -> BadgeEntity.create(UUID.fromString(userId), badge)).toList();
        badgeRepository.saveAll(badgeEntities);
    }

    private boolean hasBadges(ChallengeSolvedContext ctx) {
        return !ctx.getBadges().isEmpty();
    }
}
