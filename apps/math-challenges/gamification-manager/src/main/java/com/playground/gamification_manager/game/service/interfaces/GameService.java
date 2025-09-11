package com.playground.gamification_manager.game.service.interfaces;

import com.playground.gamification_manager.game.messaging.events.ChallengeSolvedEvent;

public interface GameService {

    void process(ChallengeSolvedEvent challengeSolved);
}
