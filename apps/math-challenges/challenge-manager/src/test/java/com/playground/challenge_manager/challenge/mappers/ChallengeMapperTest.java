package com.playground.challenge_manager.challenge.mappers;

import com.playground.challenge_manager.challenge.dataaccess.entities.ChallengeEntity;
import com.playground.challenge_manager.challenge.services.model.ChallengeStatus;
import com.playground.challenge_manager.challenge.services.model.DifficultyLevel;
import com.playground.challenge_manager.challenge.services.model.OperationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChallengeMapperTest {

    private final ChallengeMapper mapper = Mappers.getMapper(ChallengeMapper.class);

    @Test
    @DisplayName("toChallengeSolvedEvent: Should map operands correctly")
    void toChallengeSolvedEvent_Operands() {
        // given
        var firstNumber = 10;
        var secondNumber = 20;
        var entity = ChallengeEntity.create(List.of(firstNumber, secondNumber), OperationType.ADDITION, DifficultyLevel.EASY, UUID.randomUUID());
        ReflectionTestUtils.setField(entity, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(entity, "createdAt", ZonedDateTime.now());
        entity.updateStatus(ChallengeStatus.CORRECT);
        
        // when
        var event = mapper.toChallengeSolvedEvent(entity, 10, true);

        // then
        assertEquals(firstNumber, event.getFirstNumber());
        assertEquals(secondNumber, event.getSecondNumber());
    }

    @Test
    @DisplayName("toChallengeSolvedEvent: Should handle empty operands list safely")
    void toChallengeSolvedEvent_EmptyOperands() {
        // given
        var entity = ChallengeEntity.create(List.of(1, 2), OperationType.ADDITION, DifficultyLevel.EASY, UUID.randomUUID());
        ReflectionTestUtils.setField(entity, "operands", Collections.emptyList()); // Force empty list
        ReflectionTestUtils.setField(entity, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(entity, "createdAt", ZonedDateTime.now());

        // when
        var event = mapper.toChallengeSolvedEvent(entity, 0, true);

        // then
        assertEquals(0, event.getFirstNumber());
        assertEquals(0, event.getSecondNumber());
    }

    @Test
    @DisplayName("toChallengeSolvedEvent: Should handle null operands list safely")
    void toChallengeSolvedEvent_NullOperands() {
        // given
        var entity = ChallengeEntity.create(List.of(1, 2), OperationType.ADDITION, DifficultyLevel.EASY, UUID.randomUUID());
        ReflectionTestUtils.setField(entity, "operands", null); // Force null list
        ReflectionTestUtils.setField(entity, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(entity, "createdAt", ZonedDateTime.now());

        // when
        var event = mapper.toChallengeSolvedEvent(entity, 0, true);

        // then
        assertEquals(0, event.getFirstNumber());
        assertEquals(0, event.getSecondNumber());
    }

    @Test
    @DisplayName("toChallengeSolvedEvent: Should map 'correct' parameter")
    void toChallengeSolvedEvent_Correctness() {
        // given
        var entity = ChallengeEntity.create(List.of(1, 2), OperationType.ADDITION, DifficultyLevel.EASY, UUID.randomUUID());
        ReflectionTestUtils.setField(entity, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(entity, "createdAt", ZonedDateTime.now());

        // when
        var eventCorrect = mapper.toChallengeSolvedEvent(entity, 3, true);
        var eventIncorrect = mapper.toChallengeSolvedEvent(entity, 4, false);

        // then
        assertTrue(eventCorrect.isCorrect());
        assertFalse(eventIncorrect.isCorrect());
    }

    @Test
    @DisplayName("toAttemptResponse: Should map correct boolean and status")
    void toAttemptResponse_Mapping() {
        // given
        var entity = ChallengeEntity.create(List.of(1, 2), OperationType.ADDITION, DifficultyLevel.EASY, UUID.randomUUID());
        ReflectionTestUtils.setField(entity, "id", UUID.randomUUID());
        entity.updateStatus(ChallengeStatus.CORRECT);

        // when
        var response = mapper.toAttemptResponse(entity, true);

        // then
        assertEquals(entity.getId(), response.getChallengeId());
        assertEquals(ChallengeStatus.CORRECT, response.getStatus());
        assertTrue(response.getCorrect());
    }

    @Test
    @DisplayName("toAttemptResponse: Should handle null correct boolean (Expired case)")
    void toAttemptResponse_Expired() {
        // given
        var entity = ChallengeEntity.create(List.of(1, 2), OperationType.ADDITION, DifficultyLevel.EASY, UUID.randomUUID());
        ReflectionTestUtils.setField(entity, "id", UUID.randomUUID());
        entity.updateStatus(ChallengeStatus.EXPIRED);

        // when
        var response = mapper.toAttemptResponse(entity, null);

        // then
        assertEquals(entity.getId(), response.getChallengeId());
        assertEquals(ChallengeStatus.EXPIRED, response.getStatus());
        assertNull(response.getCorrect());
    }
}
