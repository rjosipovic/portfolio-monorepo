package com.playground.gamification_manager.game.service.impl.challengesolved.chain.handlers;

import com.playground.gamification_manager.game.dataaccess.domain.ScoreEntity;
import com.playground.gamification_manager.game.dataaccess.repositories.ScoreRepository;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.ChallengeSolvedContext;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.ChallengeSolvedHandler;
import com.playground.gamification_manager.game.util.MathUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaveScoreHandler implements ChallengeSolvedHandler {

    private final ScoreRepository scoreRepository;

    @Override
    public boolean shouldHandle(ChallengeSolvedContext ctx) {
        return MathUtil.isPositive(ctx.getScore());
    }

    @Override
    public void handle(ChallengeSolvedContext ctx) {
        saveScore(ctx);
    }

    private void saveScore(ChallengeSolvedContext ctx) {
        var userId = ctx.getUserId();
        var challengeAttemptId = ctx.getChallengeAttemptId();
        var score = ctx.getScore();
        var scoreEntity = new ScoreEntity();
        scoreEntity.setUserId(UUID.fromString(userId));
        scoreEntity.setScore(score);
        scoreEntity.setChallengeAttemptId(UUID.fromString(challengeAttemptId));
        scoreRepository.save(scoreEntity);
    }
}
