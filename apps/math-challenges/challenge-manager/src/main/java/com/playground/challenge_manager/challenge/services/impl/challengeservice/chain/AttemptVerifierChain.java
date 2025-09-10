package com.playground.challenge_manager.challenge.services.impl.challengeservice.chain;

import java.util.LinkedList;
import java.util.List;

public class AttemptVerifierChain {

    private final List<AttemptHandler> attemptHandlers = new LinkedList<>();

    public void addHandler(AttemptHandler attemptHandler) {
        attemptHandlers.add(attemptHandler);
    }

    public void handle(AttemptVerifierContext ctx) {
        attemptHandlers.forEach(handler -> handler.handle(ctx));
    }
}
