package com.playground.analytics_manager.outbound.services.challenge_history;

import com.playground.analytics_manager.outbound.api.dto.ChallengeResult;

import java.util.List;

public interface ChallengeHistoryService {

    List<ChallengeResult> getHistoryAttempts(String userId);
}
