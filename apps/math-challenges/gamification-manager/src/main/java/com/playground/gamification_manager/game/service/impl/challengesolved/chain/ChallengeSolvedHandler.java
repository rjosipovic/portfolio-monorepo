package com.playground.gamification_manager.game.service.impl.challengesolved.chain;

public interface ChallengeSolvedHandler {

    default boolean shouldHandle(ChallengeSolvedContext context) {
        return Boolean.TRUE;
    }
    void handle(ChallengeSolvedContext context);
}
