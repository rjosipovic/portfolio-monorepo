package com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.handlers;

import com.playground.challenge_manager.challenge.mappers.ChallengeMapper;
import com.playground.challenge_manager.challenge.messaging.producers.ChallengeSolvedProducer;
import com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.AttemptHandler;
import com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.AttemptVerifierContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PublishAttemptHandler implements AttemptHandler {

    private final ChallengeSolvedProducer challengeSolvedProducer;
    private final ChallengeMapper challengeMapper;

    @Override
    public void handle(AttemptVerifierContext ctx) {
        log.info("Publishing attempt: {}", ctx.getChallengeAttempt());
        var challengeAttempt = ctx.getChallengeAttempt();
        var challengeSolved = challengeMapper.toChallengeSolvedEvent(challengeAttempt);
        challengeSolvedProducer.publishChallengeSolvedMessage(challengeSolved);
    }
}
