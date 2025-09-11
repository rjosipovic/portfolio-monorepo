package com.playground.analytics_manager.outbound.services.challenge_history;

import com.playground.analytics_manager.dataaccess.repository.ChallengeRepository;
import com.playground.analytics_manager.outbound.api.dto.ChallengeResult;
import com.playground.analytics_manager.outbound.services.challenge_history.util.MathUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChallengeHistoryServiceImpl implements ChallengeHistoryService {

    private final ChallengeRepository challengeRepository;

    @Override
    public List<ChallengeResult> getHistoryAttempts(String userId) {
        var attemptEntities = challengeRepository.findByUserAttempt_UserId(UUID.fromString(userId));
        if (!attemptEntities.isEmpty()) {
            return attemptEntities.stream()
                    .map(e -> ChallengeResult.builder()
                            .alias(e.getUserAttempt().getUser().getAlias())
                            .firstNumber(e.getFirstNumber())
                            .secondNumber(e.getSecondNumber())
                            .guess(e.getUserAttempt().getResultAttempt())
                            .correctResult(MathUtil.calculateResult(e.getFirstNumber(), e.getSecondNumber(), e.getGame()))
                            .correct(e.getUserAttempt().getCorrect())
                            .game(e.getGame())
                            .build())
                    .toList();
        }
        return List.of();
    }
}
