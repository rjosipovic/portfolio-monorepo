package com.playground.gamification_manager.game.service.impl.leaderboard;

import com.playground.gamification_manager.game.dataaccess.domain.BadgeEntity;
import com.playground.gamification_manager.game.dataaccess.domain.BadgeType;
import com.playground.gamification_manager.game.dataaccess.domain.UserScore;
import com.playground.gamification_manager.game.dataaccess.repositories.BadgeRepository;
import com.playground.gamification_manager.game.service.interfaces.AliasService;
import com.playground.gamification_manager.game.service.interfaces.LeaderBoardService;
import com.playground.gamification_manager.game.service.model.LeaderBoardItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaderBoardServiceImpl implements LeaderBoardService {

    private final BadgeRepository badgeRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final LeaderBoardCacheConfiguration leaderBoardCacheConfiguration;
    private final AliasService aliasService;

    @Override
    public List<LeaderBoardItem> getLeaderBoard() {

        var leaders = getLeaders();
        var userIds = leaders.stream().map(UserScore::getUserId).collect(Collectors.toSet());
        var aliasMap = buildAliasMap(userIds);
        return mapToLeaderBoard(leaders, aliasMap);
    }

    private List<UserScore> getLeaders() {
        var key = leaderBoardCacheConfiguration.getKey();
        var startIdx = 0;
        var endIdx = leaderBoardCacheConfiguration.getSize() - 1;
        var zSetOps = redisTemplate.opsForZSet();
        var tuples = zSetOps.reverseRangeWithScores(key, startIdx, endIdx);
        if (Objects.isNull(tuples)) {
            return List.of();
        }
        return tuples.stream()
                .filter(t -> Objects.nonNull(t.getValue()) && Objects.nonNull(t.getScore()))
                .map(t -> new UserScore(UUID.fromString(t.getValue().toString()), t.getScore().longValue()))
                .collect(Collectors.toList());
    }

    private List<LeaderBoardItem> mapToLeaderBoard(List<UserScore> leaders, Map<String, String> aliasMap) {
        return leaders.stream()
                .map(leader -> mapToLeaderBoardItem(leader, aliasMap.get(leader.getUserId().toString())))
                .toList();
    }

    private LeaderBoardItem mapToLeaderBoardItem(UserScore leader, String alias) {
        var userId = leader.getUserId();
        var totalScore = leader.getTotalScore();
        var badges = getBadgesForUser(userId);
        return LeaderBoardItem.builder()
                .alias(Objects.nonNull(alias) ? alias : "Unknown")
                .totalScore(totalScore)
                .badges(badges)
                .build();
    }

    private Set<BadgeType> getBadgesForUser(UUID userId) {
        return badgeRepository.findAllByUserId(userId)
                .stream()
                .map(BadgeEntity::getBadgeType)
                .collect(Collectors.toSet());
    }

    private Map<String, String> buildAliasMap(Set<UUID> userIds) {
        return aliasService.getAlias(userIds);
    }
}
