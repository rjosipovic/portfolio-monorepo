package com.playground.gamification_manager.game.service.impl.challengesolved.chain.config;

import com.playground.gamification_manager.game.service.impl.challengesolved.chain.ChallengeSolvedChain;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.handlers.BadgesHandler;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.handlers.DifficultyHandler;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.handlers.SaveBadgesHandler;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.handlers.SaveScoreHandler;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.handlers.ScoreHandler;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.handlers.TotalScoreHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ChallengeSolvedChainConfiguration {

    private final DifficultyHandler difficultyHandler;
    private final ScoreHandler scoreHandler;
    private final BadgesHandler badgesHandler;
    private final SaveScoreHandler saveScoreHandler;
    private final SaveBadgesHandler saveBadgesHandler;
    private final TotalScoreHandler totalScoreHandler;

    @Bean
    public ChallengeSolvedChain challengeSolvedChain() {
        var chain = new ChallengeSolvedChain();
        chain.addHandler(difficultyHandler);
        chain.addHandler(scoreHandler);
        chain.addHandler(badgesHandler);
        chain.addHandler(saveScoreHandler);
        chain.addHandler(saveBadgesHandler);
        chain.addHandler(totalScoreHandler);
        return chain;
    }
}
