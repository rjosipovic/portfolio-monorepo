package com.playground.gamification_manager.game.service.impl;

import com.playground.gamification_manager.game.messaging.events.ChallengeSolvedEvent;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.ChallengeSolvedChain;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.ChallengeSolvedContext;
import com.playground.gamification_manager.game.service.interfaces.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final ChallengeSolvedChain challengeSolvedChain;

    @Override
    @Transactional
    public void process(ChallengeSolvedEvent challengeSolved) {
        var ctx = new ChallengeSolvedContext(challengeSolved);
        challengeSolvedChain.handle(ctx);
    }
}
