package com.playground.analytics_manager.outbound.services.user_statistics;

import com.playground.analytics_manager.dataaccess.entity.UserEntity;
import com.playground.analytics_manager.dataaccess.repository.ChallengeRepository;
import com.playground.analytics_manager.dataaccess.repository.UserRepository;
import com.playground.analytics_manager.outbound.api.dto.UserSuccessRate;
import com.playground.analytics_manager.outbound.errors.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserStatisticsServiceImpl implements UserStatisticsService {

    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;

    @Override
    public UserSuccessRate getUserStatistics(String userId) {
        var userEntity = getUserEntity(userId);
        var alias = userEntity.getAlias();
        var attempts = challengeRepository.findByUserAttempt_UserId(userEntity.getId());
        var userSuccessRate = new UserSuccessRate(alias);

        attempts.forEach(a -> {
            var game = a.getGame();
            var difficulty = a.getDifficulty();
            var userAttempt = a.getUserAttempt();
            var correct = userAttempt.getCorrect();
            if (correct) {
                userSuccessRate.processSuccessAttempt(game, difficulty);
            } else {
                userSuccessRate.processFailAttempt(game, difficulty);
            }
        });
        return userSuccessRate;
    }

    private UserEntity getUserEntity(String userId) {
        return userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UserNotFoundException(String.format("User %s not found", userId)));
    }
}
