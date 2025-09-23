package com.playground.gamification_manager.game.service.impl.challengesolved.chain.handlers;

import com.playground.gamification_manager.game.service.impl.challengesolved.chain.ChallengeSolvedContext;
import com.playground.gamification_manager.game.service.impl.leaderboard.LeaderBoardCacheConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TotalScoreHandlerTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ZSetOperations<String, Object> zSetOperations;

    @Mock
    private LeaderBoardCacheConfiguration leaderBoardCacheConfiguration;

    @InjectMocks
    private TotalScoreHandler totalScoreHandler;

    @Test
    void shouldHandle_returnsTrueForPositiveScore() {
        //given
        var ctx = mock(ChallengeSolvedContext.class);
        when(ctx.getScore()).thenReturn(10);

        //when
        var shouldHandle = totalScoreHandler.shouldHandle(ctx);

        //then
        assertTrue(shouldHandle);
    }

    @Test
    void shouldHandle_returnsFalseForNonPositiveScore() {
        //given
        var ctx = mock(ChallengeSolvedContext.class);
        when(ctx.getScore()).thenReturn(0);

        //when
        var shouldHandle = totalScoreHandler.shouldHandle(ctx);

        //then
        assertFalse(shouldHandle);
    }

    @Test
    void handle_updatesLeaderboardWithTotalScore() {
        //given
        var userId = UUID.randomUUID().toString();
        var totalScore = 123;
        var key = "leaderboard";

        var ctx = mock(ChallengeSolvedContext.class);
        when(ctx.getUserId()).thenReturn(userId);
        when(ctx.getTotalScore()).thenReturn(totalScore);
        when(leaderBoardCacheConfiguration.getKey()).thenReturn(key);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        //when
        totalScoreHandler.handle(ctx);

        //then
        verify(zSetOperations).add(key, userId, totalScore);
    }
}