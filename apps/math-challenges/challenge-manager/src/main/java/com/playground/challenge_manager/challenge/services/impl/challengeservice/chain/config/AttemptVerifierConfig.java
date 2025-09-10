package com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.config;

import com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.AttemptVerifierChain;
import com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.handlers.AttemptResultHandler;
import com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.handlers.CheckResultHandler;
import com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.handlers.PublishAttemptHandler;
import com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.handlers.SaveAttemptHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AttemptVerifierConfig {

    private final CheckResultHandler checkResultHandler;
    private final SaveAttemptHandler saveAttemptHandler;
    private final PublishAttemptHandler publishAttemptHandler;

    @Bean
    public AttemptVerifierChain attemptVerifierChain() {
        var chain = new AttemptVerifierChain();
        chain.addHandler(checkResultHandler);
        chain.addHandler(saveAttemptHandler);
        chain.addHandler(publishAttemptHandler);
        chain.addHandler(new AttemptResultHandler());
        return chain;
    }
}
