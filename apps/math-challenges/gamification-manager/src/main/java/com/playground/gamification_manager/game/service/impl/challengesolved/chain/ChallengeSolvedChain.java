package com.playground.gamification_manager.game.service.impl.challengesolved.chain;

import java.util.LinkedList;
import java.util.List;

public class ChallengeSolvedChain {

    private final List<ChallengeSolvedHandler> handlers = new LinkedList<>();

    public void addHandler(ChallengeSolvedHandler handler) {
        handlers.add(handler);
    }

    public void handle(ChallengeSolvedContext ctx) {
        handlers.stream()
                .filter(handler -> handler.shouldHandle(ctx))
                .forEach(handler -> handler.handle(ctx));
    }
}
