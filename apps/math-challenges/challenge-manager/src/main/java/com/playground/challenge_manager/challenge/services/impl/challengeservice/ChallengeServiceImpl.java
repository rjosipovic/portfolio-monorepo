package com.playground.challenge_manager.challenge.services.impl.challengeservice;

import com.playground.challenge_manager.challenge.api.dto.AttemptResponse;
import com.playground.challenge_manager.challenge.api.dto.ChallengeResponse;
import com.playground.challenge_manager.challenge.dataaccess.entities.ChallengeEntity;
import com.playground.challenge_manager.challenge.dataaccess.repositories.ChallengeRepository;
import com.playground.challenge_manager.challenge.mappers.ChallengeMapper;
import com.playground.challenge_manager.challenge.messaging.producers.ChallengeAttemptProducer;
import com.playground.challenge_manager.challenge.services.impl.ChallengeEventProducer;
import com.playground.challenge_manager.challenge.services.impl.SseService;
import com.playground.challenge_manager.challenge.services.interfaces.ChallengeGeneratorService;
import com.playground.challenge_manager.challenge.services.interfaces.ChallengeService;
import com.playground.challenge_manager.challenge.services.model.ChallengeStatus;
import com.playground.challenge_manager.challenge.services.model.commands.AttemptVerificationCommand;
import com.playground.challenge_manager.challenge.services.model.commands.ChallengeCreationCommand;
import com.playground.challenge_manager.challenge.services.model.commands.GetChallengeQuery;
import com.playground.challenge_manager.challenge.services.model.commands.SubscribeToChallengeCommand;
import com.playground.challenge_manager.errors.exceptions.enums.ErrorCode;
import com.playground.challenge_manager.errors.exceptions.specific.ChallengeDataCorruptedException;
import com.playground.challenge_manager.errors.exceptions.specific.ChallengeSubscriptionException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChallengeServiceImpl implements ChallengeService {

    private final ChallengeGeneratorService challengeGeneratorService;
    private final ChallengeRepository challengeRepository;
    private final ChallengeMapper challengeMapper;
    private final ChallengeEventProducer challengeEventProducer;
    private final ChallengeAttemptProducer challengeAttemptProducer;
    private final SseService sseService;

    @Override
    @Transactional
    public UUID create(ChallengeCreationCommand command) {
        var operation = command.getOperation();
        var difficulty = command.getDifficulty();
        var operandsCount = command.getOperandCount();
        var userId = command.getUserId();

        var operands = challengeGeneratorService.generate(difficulty, operandsCount);

        var challengeEntity = ChallengeEntity.create(operands, operation, difficulty, userId);
        var savedChallenge = challengeRepository.save(challengeEntity);
        var challengeId = savedChallenge.getId();

        challengeEventProducer.publishChallengeCreated(challengeId);

        return challengeId;
    }

    @Override
    public ChallengeResponse getChallenge(GetChallengeQuery query) {
        var challengeId = query.getChallengeId();
        var userId = query.getUserId();
        return challengeRepository.findOneByIdAndUserId(challengeId, userId)
                .map(challengeMapper::toChallengeResponse)
                .orElseThrow(() -> new EntityNotFoundException("Challenge not found"));
    }

    @Override
    @Transactional
    public AttemptResponse submitAttempt(AttemptVerificationCommand command) {
        var challengeEntity = challengeRepository.findOneByIdAndUserId(command.getChallengeId(), command.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Challenge not found"));

        ensureChallengeIsPlayable(challengeEntity);

        if (isExpired(challengeEntity.getExpiresAt())) {
            challengeEntity.updateStatus(ChallengeStatus.EXPIRED);
            challengeRepository.save(challengeEntity);
            return challengeMapper.toAttemptResponse(challengeEntity, null);
        }

        var isCorrect = Objects.equals(command.getGuess(), challengeEntity.getCorrectAnswer());
        var status = isCorrect ? ChallengeStatus.CORRECT : ChallengeStatus.INCORRECT;

        challengeEntity.updateStatus(status);
        challengeEntity.updateAttemptedAt(ZonedDateTime.now());
        var saved = challengeRepository.save(challengeEntity);

        challengeAttemptProducer.publishChallengeSolvedMessage(challengeMapper.toChallengeSolvedEvent(saved, command.getGuess(), isCorrect));

        return challengeMapper.toAttemptResponse(saved, isCorrect);
    }

    @Override
    public SseEmitter subscribeToChallenge(SubscribeToChallengeCommand command) {
        var challengeId = command.getChallengeId();
        var userId = command.getUserId();

        return challengeRepository.findOneByIdAndUserId(challengeId, userId)
                .map(sseService::subscribe)
                // In ChallengeServiceImplementation.java
                .orElseThrow(() -> new ChallengeSubscriptionException(
                        ErrorCode.CHALLENGE_NOT_FOUND_FOR_SUBSCRIPTION,
                        "Challenge with id " + challengeId + " not found or access denied."
                ));
    }

    private void ensureChallengeIsPlayable(ChallengeEntity challengeEntity) {
        if (!Objects.equals(challengeEntity.getStatus(), ChallengeStatus.PENDING)) {
            throw new ChallengeDataCorruptedException(ErrorCode.CONFLICT, "Challenge is not in valid state for verification");
        }
        var correctAnswer = challengeEntity.getCorrectAnswer();

        if (Objects.isNull(correctAnswer)) {
            throw new ChallengeDataCorruptedException(ErrorCode.INTERNAL_SERVER_ERROR, "Challenge has no correct answer.");
        }
    }

    private boolean isExpired(ZonedDateTime expiresAt) {
        return expiresAt.isBefore(ZonedDateTime.now());
    }
}
