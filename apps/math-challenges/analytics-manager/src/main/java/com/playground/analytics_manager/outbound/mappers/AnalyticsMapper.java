package com.playground.analytics_manager.outbound.mappers;

import com.playground.analytics_manager.dataaccess.entity.ChallengeEntity;
import com.playground.analytics_manager.outbound.api.dto.ChallengeResult;
import com.playground.analytics_manager.outbound.services.challenge_history.util.MathUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = { MathUtil.class })
public interface AnalyticsMapper {

    @Mapping(source = "userAttempt.user.alias", target = "alias")
    @Mapping(source = "userAttempt.resultAttempt", target = "guess")
    @Mapping(source = "userAttempt.correct", target = "correct")
    @Mapping(target = "correctResult", ignore = true)
    ChallengeResult toDto(ChallengeEntity entity);
}
