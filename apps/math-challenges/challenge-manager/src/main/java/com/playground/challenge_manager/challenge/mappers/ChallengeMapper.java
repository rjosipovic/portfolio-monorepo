package com.playground.challenge_manager.challenge.mappers;

import com.playground.challenge_manager.challenge.api.dto.AttemptResponse;
import com.playground.challenge_manager.challenge.api.dto.ChallengeResponse;
import com.playground.challenge_manager.challenge.dataaccess.entities.ChallengeEntity;
import com.playground.challenge_manager.challenge.messaging.events.ChallengeSolvedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChallengeMapper {

    @Mapping(source = "entity.id", target = "challengeAttemptId")
    @Mapping(source = "entity.userId", target = "userId")
    @Mapping(target = "firstNumber", expression = "java(entity.getOperands() != null && entity.getOperands().size() > 0 ? entity.getOperands().get(0) : 0)")
    @Mapping(target = "secondNumber", expression = "java(entity.getOperands() != null && entity.getOperands().size() > 1 ? entity.getOperands().get(1) : 0)")
    @Mapping(source = "guess", target = "resultAttempt") // Map the user's guess
    @Mapping(source = "correct", target = "correct")
    @Mapping(target = "game", expression = "java(entity.getOperationType().name().toLowerCase())")
    @Mapping(source = "entity.difficulty", target = "difficulty")
    @Mapping(source = "entity.attemptedAt", target = "attemptDate")
    ChallengeSolvedEvent toChallengeSolvedEvent(ChallengeEntity entity, Integer guess, Boolean correct);

    @Mapping(source = "operationType", target = "operation")
    ChallengeResponse toChallengeResponse(ChallengeEntity entity);

    @Mapping(source = "entity.id", target = "challengeId")
    @Mapping(source = "correct", target = "correct")
    AttemptResponse toAttemptResponse(ChallengeEntity entity, Boolean correct);

}
