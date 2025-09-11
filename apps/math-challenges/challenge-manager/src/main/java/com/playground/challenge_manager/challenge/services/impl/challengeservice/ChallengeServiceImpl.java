package com.playground.challenge_manager.challenge.services.impl.challengeservice;

import com.playground.challenge_manager.challenge.api.dto.ChallengeAttemptDTO;
import com.playground.challenge_manager.challenge.api.dto.ChallengeResultDTO;
import com.playground.challenge_manager.challenge.dataaccess.repositories.ChallengeAttemptRepository;
import com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.AttemptVerifierChain;
import com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.AttemptVerifierContext;
import com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.util.MathUtil;
import com.playground.challenge_manager.challenge.services.interfaces.AttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChallengeServiceImpl implements AttemptService {

    private final AttemptVerifierChain attemptVerifierChain;
    private final ChallengeAttemptRepository challengeAttemptRepository;

    @Override
    @Transactional
    public ChallengeResultDTO verifyAttempt(ChallengeAttemptDTO attempt) {
        var ctx = new AttemptVerifierContext(attempt);
        attemptVerifierChain.handle(ctx);
        return ctx.getChallengeResult();
    }

    @Override
    public List<ChallengeResultDTO> findLast10AttemptsForUser(UUID userId) {
        return challengeAttemptRepository.findLast10AttemptsByUser(userId)
                .stream()
                .map(e -> ChallengeResultDTO.builder()
                        .userId(e.getUserId().toString())
                        .firstNumber(e.getFirstNumber())
                        .secondNumber(e.getSecondNumber())
                        .guess(e.getResultAttempt())
                        .correctResult(MathUtil.calculateResult(e.getFirstNumber(), e.getSecondNumber(), e.getGame()))
                        .correct(e.isCorrect())
                        .game(e.getGame())
                        .build())
                .toList();
    }
}
