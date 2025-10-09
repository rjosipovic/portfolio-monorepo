package com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.handlers;

import com.playground.challenge_manager.challenge.dataaccess.repositories.ChallengeAttemptRepository;
import com.playground.challenge_manager.challenge.mappers.ChallengeMapper;
import com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.AttemptHandler;
import com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.AttemptVerifierContext;
import com.playground.challenge_manager.challenge.services.model.ChallengeAttempt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveAttemptHandler implements AttemptHandler {

    private final ChallengeAttemptRepository challengeAttemptRepository;
    private final ChallengeMapper challengeMapper;

    @Override
    public void handle(AttemptVerifierContext ctx) {
        var challengeAttempt = ctx.getChallengeAttempt();
        var savedAttempt = saveAttempt(challengeAttempt);
        ctx.setChallengeAttempt(savedAttempt);
    }

    private ChallengeAttempt saveAttempt(ChallengeAttempt challengeAttempt) {
        var challengeAttemptEntity = challengeMapper.toEntity(challengeAttempt);
        var savedAttempt = challengeAttemptRepository.saveAndFlush(challengeAttemptEntity);
        var challengeAttemptId = savedAttempt.getId();
        var attemptDate = savedAttempt.getAttemptDate();
        return challengeAttempt.toBuilder().challengeAttemptId(challengeAttemptId).attemptDate(attemptDate).build();
    }
}
