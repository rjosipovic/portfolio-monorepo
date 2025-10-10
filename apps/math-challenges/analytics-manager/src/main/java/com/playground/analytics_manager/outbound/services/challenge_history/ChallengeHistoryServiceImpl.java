package com.playground.analytics_manager.outbound.services.challenge_history;

import com.playground.analytics_manager.dataaccess.entity.ChallengeEntity;
import com.playground.analytics_manager.dataaccess.repository.ChallengeRepository;
import com.playground.analytics_manager.outbound.api.dto.ChallengeResult;
import com.playground.analytics_manager.outbound.mappers.AnalyticsMapper;
import com.playground.analytics_manager.outbound.services.challenge_history.util.MathUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChallengeHistoryServiceImpl implements ChallengeHistoryService {

    private final ChallengeRepository challengeRepository;
    private final AnalyticsMapper analyticsMapper;

    @Override
    public List<ChallengeResult> getHistoryAttempts(String userId) {
        var attemptEntities = challengeRepository.findByUserAttempt_UserId(UUID.fromString(userId));
        if (!attemptEntities.isEmpty()) {
            return attemptEntities.stream()
                    .map(this::build)
                    .toList();
        }
        return List.of();
    }

    private ChallengeResult build(ChallengeEntity entity) {
        var mapped = analyticsMapper.toDto(entity);
        var correctResult = MathUtil.calculateResult(entity.getFirstNumber(), entity.getSecondNumber(), entity.getGame());
        return mapped.toBuilder().correctResult(correctResult).build();
    }
}
