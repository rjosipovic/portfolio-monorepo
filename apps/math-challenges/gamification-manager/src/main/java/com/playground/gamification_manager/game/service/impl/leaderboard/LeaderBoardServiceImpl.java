package com.playground.gamification_manager.game.service.impl.leaderboard;

import com.playground.gamification_manager.game.dataaccess.domain.BadgeEntity;
import com.playground.gamification_manager.game.dataaccess.repositories.BadgeRepository;
import com.playground.gamification_manager.game.service.interfaces.LeaderBoardService;
import com.playground.gamification_manager.game.service.model.LeaderBoardItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaderBoardServiceImpl implements LeaderBoardService {

    private final BadgeRepository badgeRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final LeaderBoardConfiguration leaderBoardConfiguration;

    @Override
    public List<LeaderBoardItem> getLeaderBoard() {

        var leaders = getLeaders();

        return leaders.stream()
                .filter(leader -> Objects.nonNull(leader.getValue()) && Objects.nonNull(leader.getScore()))
                .map(leader -> {
                    var userId = UUID.fromString(leader.getValue().toString());
                    var totalScore = leader.getScore();
                    var badges = badgeRepository.findAllByUserId(userId)
                            .stream()
                            .map(BadgeEntity::getBadgeType)
                            .collect(Collectors.toSet());
                    return LeaderBoardItem.builder()
                            .userId(userId)
                            .totalScore(totalScore.longValue())
                            .badges(badges)
                            .build();
                })
                .toList();
    }

    private Set<ZSetOperations.TypedTuple<Object>> getLeaders() {
        var key = leaderBoardConfiguration.getKey();
        var startIdx = 0;
        var endIdx = leaderBoardConfiguration.getSize() - 1;
        var zSetOps = redisTemplate.opsForZSet();
        return zSetOps.reverseRangeWithScores(key, startIdx, endIdx);
    }
}
