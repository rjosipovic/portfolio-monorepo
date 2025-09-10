package com.playground.gamification_manager.game.service.impl.challengesolved.chain.handlers;

import com.playground.gamification_manager.game.dataaccess.domain.BadgeEntity;
import com.playground.gamification_manager.game.dataaccess.domain.BadgeType;
import com.playground.gamification_manager.game.dataaccess.repositories.BadgeRepository;
import com.playground.gamification_manager.game.service.impl.badge.BadgesContext;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.ChallengeSolvedContext;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.ChallengeSolvedHandler;
import com.playground.gamification_manager.game.service.interfaces.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BadgesHandler implements ChallengeSolvedHandler {

    private final BadgeRepository badgeRepository;
    private final BadgeService badgeService;

    @Override
    public boolean shouldHandle(ChallengeSolvedContext ctx) {
        return ctx.isCorrect();
    }

    @Override
    public void handle(ChallengeSolvedContext ctx) {
        var score = ctx.getScore();
        var firstNumber = ctx.getFirstNumber();
        var secondNumber = ctx.getSecondNumber();
        var currentScore = ctx.getTotalScore();
        var currentBadges = getCurrentBadges(ctx);

        var badgesCtx = BadgesContext.builder()
                .newScore(score)
                .currentScore(currentScore)
                .currentBadges(currentBadges)
                .firstNumber(firstNumber)
                .secondNumber(secondNumber)
                .build();
        var newBadges = badgeService.determineBadges(badgesCtx);

        ctx.addBadges(newBadges);
    }

    private Set<BadgeType> getCurrentBadges(ChallengeSolvedContext ctx) {
        var userId = ctx.getUserId();
        return badgeRepository.findAllByUserId(UUID.fromString(userId))
                .stream().map(BadgeEntity::getBadgeType)
                .collect(Collectors.toSet());
    }
}
