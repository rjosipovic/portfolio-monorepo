package com.playground.challenge_manager.challenge.mappers;

import com.playground.challenge_manager.challenge.api.dto.ChallengeAttemptDTO;
import com.playground.challenge_manager.challenge.api.dto.ChallengeResultDTO;
import com.playground.challenge_manager.challenge.dataaccess.entities.ChallengeAttemptEntity;
import com.playground.challenge_manager.challenge.messaging.events.ChallengeSolvedEvent;
import com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.util.MathUtil;
import com.playground.challenge_manager.challenge.services.model.ChallengeAttempt;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring", imports = { MathUtil.class })
public interface ChallengeMapper {

    ChallengeAttemptEntity toEntity(ChallengeAttempt domain);

    ChallengeSolvedEvent toChallengeSolvedEvent(ChallengeAttempt domain);

    @Mapping(source = "guess", target = "resultAttempt")
    @Mapping(target = "correct", ignore = true)
    @Mapping(target = "difficulty", ignore = true)
    ChallengeAttempt toChallengeAttempt(ChallengeAttemptDTO dto);

    @Mapping(source = "resultAttempt", target = "guess")
    @Mapping(target = "correctResult", ignore = true)
    ChallengeResultDTO toResultDto(ChallengeAttemptEntity entity);

    @Mapping(source = "resultAttempt", target = "guess")
    @Mapping(target = "correctResult", ignore = true)
    ChallengeResultDTO toResultDto(ChallengeAttempt attempt);

    /**
     * This method is a custom object factory for creating ChallengeAttemptEntity instances.
     * MapStruct will use this method instead of a default constructor whenever it needs to
     * create an entity from a ChallengeAttempt domain object.
     * This allows us to enforce the use of our static factory method on the entity.
     */
    @ObjectFactory
    default ChallengeAttemptEntity createEntity(ChallengeAttempt domain) {
        return ChallengeAttemptEntity.create(
                domain.getUserId(),
                domain.getFirstNumber(),
                domain.getSecondNumber(),
                domain.getResultAttempt(),
                domain.getCorrect(),
                domain.getGame(),
                domain.getDifficulty()
        );
    }
}
