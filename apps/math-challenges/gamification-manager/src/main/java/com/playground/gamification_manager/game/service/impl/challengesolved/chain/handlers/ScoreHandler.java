package com.playground.gamification_manager.game.service.impl.challengesolved.chain.handlers;

import com.playground.gamification_manager.game.dataaccess.repositories.ScoreRepository;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.ChallengeSolvedContext;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.ChallengeSolvedHandler;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.config.DifficultyLevelsConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScoreHandler implements ChallengeSolvedHandler {

    private final DifficultyLevelsConfiguration difficultyLevelsConfiguration;
    private final ScoreRepository scoreRepository;

    @Override
    public boolean shouldHandle(ChallengeSolvedContext ctx) {
        return ctx.isCorrect();
    }

    @Override
    public void handle(ChallengeSolvedContext ctx) {
        var difficulty = ctx.getDifficulty();
        var score = getScoreFromDifficulty(difficulty);
        var totalScore = getTotalScore(ctx);
        ctx.setScore(score);
        ctx.setTotalScore(totalScore);
    }

    private int getScoreFromDifficulty(String difficulty) {
        return Optional.ofNullable(difficultyLevelsConfiguration.getScoreMap().get(difficulty)).orElseGet(() -> 0);
    }

    private int getTotalScore(ChallengeSolvedContext ctx) {
        var userId = ctx.getUserId();
        return Optional.ofNullable(scoreRepository.totalScoreByUserId(UUID.fromString(userId)))
                .orElse(0);
    }
}
