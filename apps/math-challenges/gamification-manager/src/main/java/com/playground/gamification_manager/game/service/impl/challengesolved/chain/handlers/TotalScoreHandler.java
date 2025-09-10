package com.playground.gamification_manager.game.service.impl.challengesolved.chain.handlers;

import com.playground.gamification_manager.game.service.impl.challengesolved.chain.ChallengeSolvedContext;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.ChallengeSolvedHandler;
import com.playground.gamification_manager.game.service.impl.leaderboard.LeaderBoardConfiguration;
import com.playground.gamification_manager.game.util.MathUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TotalScoreHandler implements ChallengeSolvedHandler {

    private final LeaderBoardConfiguration leaderBoardConfiguration;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean shouldHandle(ChallengeSolvedContext ctx) {
        return MathUtil.isPositive(ctx.getScore());
    }

    @Override
    public void handle(ChallengeSolvedContext ctx) {
        var userId = ctx.getUserId();
        var totalScore = ctx.getTotalScore();
        var score = ctx.getScore();
        var newTotalScore = totalScore + score;
        ctx.setTotalScore(newTotalScore);
        updateLeaderboard(userId, newTotalScore);
    }

    private void updateLeaderboard(String userId, long totalScore) {
        var zSetOps = redisTemplate.opsForZSet();
        var key = leaderBoardConfiguration.getKey();
        zSetOps.add(key, userId, totalScore);
    }
}
