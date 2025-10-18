package com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.handlers;

import com.playground.challenge_manager.challenge.api.dto.ChallengeResultDTO;
import com.playground.challenge_manager.challenge.mappers.ChallengeMapper;
import com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.AttemptHandler;
import com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.AttemptVerifierContext;
import com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.util.MathUtil;
import com.playground.challenge_manager.challenge.services.model.ChallengeAttempt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttemptResultHandler implements AttemptHandler {

    private final ChallengeMapper challengeMapper;

    @Override
    public void handle(AttemptVerifierContext ctx) {
        var challengeAttempt = ctx.getChallengeAttempt();
        var challengeResult = build(challengeAttempt);
        ctx.setChallengeResult(challengeResult);
    }

    private ChallengeResultDTO build(ChallengeAttempt attempt) {
        var result = challengeMapper.toResultDto(attempt);
        var correctResult = MathUtil.calculateResult(result.getFirstNumber(), result.getSecondNumber(), result.getGame());
        return result.toBuilder().correctResult(correctResult).build();
    }
}
