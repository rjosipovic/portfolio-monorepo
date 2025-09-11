package com.playground.analytics_manager.inbound.challenge;

import com.playground.analytics_manager.inbound.messaging.events.ChallengeSolvedEvent;

public interface ChallengeService {

    void process(ChallengeSolvedEvent event);
}
