package com.playground.challenge_manager.challenge.services.impl.challengeservice;

import com.playground.challenge_manager.challenge.api.dto.AttemptResponse;
import com.playground.challenge_manager.challenge.api.dto.ChallengeResponse;
import com.playground.challenge_manager.challenge.dataaccess.entities.ChallengeEntity;
import com.playground.challenge_manager.challenge.dataaccess.repositories.ChallengeRepository;
import com.playground.challenge_manager.challenge.mappers.ChallengeMapper;
import com.playground.challenge_manager.challenge.messaging.events.ChallengeSolvedEvent;
import com.playground.challenge_manager.challenge.messaging.producers.ChallengeAttemptProducer;
import com.playground.challenge_manager.challenge.services.impl.ChallengeEventProducer;
import com.playground.challenge_manager.challenge.services.impl.SseService;
import com.playground.challenge_manager.challenge.services.interfaces.ChallengeGeneratorService;
import com.playground.challenge_manager.challenge.services.model.ChallengeStatus;
import com.playground.challenge_manager.challenge.services.model.DifficultyLevel;
import com.playground.challenge_manager.challenge.services.model.OperationType;
import com.playground.challenge_manager.challenge.services.model.commands.AttemptVerificationCommand;
import com.playground.challenge_manager.challenge.services.model.commands.ChallengeCreationCommand;
import com.playground.challenge_manager.challenge.services.model.commands.GetChallengeQuery;
import com.playground.challenge_manager.challenge.services.model.commands.SubscribeToChallengeCommand;
import com.playground.challenge_manager.errors.exceptions.enums.ErrorCode;
import com.playground.challenge_manager.errors.exceptions.specific.ChallengeDataCorruptedException;
import com.playground.challenge_manager.errors.exceptions.specific.ChallengeSubscriptionException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengeServiceImpl Test")
class ChallengeServiceImplTest {

    @Mock
    private ChallengeGeneratorService challengeGeneratorService;
    @Mock
    private ChallengeRepository challengeRepository;
    @Mock
    SseService sseService;
    @Mock
    private ChallengeEventProducer challengeEventProducer;
    @Mock
    private ChallengeAttemptProducer challengeAttemptProducer;
    @Spy
    private ChallengeMapper challengeMapper = Mappers.getMapper(ChallengeMapper.class);

    @InjectMocks
    private ChallengeServiceImpl challengeService;

    @Test
    @DisplayName("create: Should generate operands, save entity, and publish event")
    void create_Success() {
        // given
        var userId = UUID.randomUUID();
        var command = ChallengeCreationCommand.builder()
                .userId(userId)
                .difficulty(DifficultyLevel.EASY)
                .operation(OperationType.ADDITION)
                .operandCount(2)
                .build();

        var operands = List.of(1, 2);
        when(challengeGeneratorService.generate(DifficultyLevel.EASY, 2)).thenReturn(operands);

        var savedEntity = ChallengeEntity.create(operands, OperationType.ADDITION, DifficultyLevel.EASY, userId);
        var generatedId = UUID.randomUUID();
        var createdAt = ZonedDateTime.now();
        ReflectionTestUtils.setField(savedEntity, "id", generatedId); // Use reflection to set ID
        ReflectionTestUtils.setField(savedEntity, "createdAt", createdAt); // Use reflection to set createdAt

        when(challengeRepository.save(any(ChallengeEntity.class))).thenReturn(savedEntity);

        // when
        var resultId = challengeService.create(command);

        // then
        assertEquals(generatedId, resultId);
        verify(challengeEventProducer).publishChallengeCreated(generatedId);
        
        var captor = ArgumentCaptor.forClass(ChallengeEntity.class);
        verify(challengeRepository).save(captor.capture());
        var capturedEntity = captor.getValue();
        assertEquals(userId, capturedEntity.getUserId());
        assertEquals(operands, capturedEntity.getOperands());
        assertEquals(OperationType.ADDITION, capturedEntity.getOperationType());
        assertEquals(DifficultyLevel.EASY, capturedEntity.getDifficulty());
    }

    @Test
    @DisplayName("getChallenge: Should return mapped response when found")
    void getChallenge_Success() {
        // given
        var challengeId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var entity = ChallengeEntity.create(List.of(1, 2), OperationType.ADDITION, DifficultyLevel.EASY, userId);
        entity.updateStatus(ChallengeStatus.PENDING);
        ReflectionTestUtils.setField(entity, "id", challengeId);

        var query = GetChallengeQuery.builder().challengeId(challengeId).userId(userId).build();
        when(challengeRepository.findOneByIdAndUserId(challengeId, userId)).thenReturn(Optional.of(entity));
        var expectedResponse = ChallengeResponse.builder()
                .id(challengeId)
                .status(ChallengeStatus.PENDING)
                .operands(List.of(1, 2))
                .operation(OperationType.ADDITION)
                .difficulty(DifficultyLevel.EASY)
                .build();

        // when
        var result = challengeService.getChallenge(query);

        // then
        assertEquals(expectedResponse, result);
        verify(challengeRepository).findOneByIdAndUserId(challengeId, userId);
    }

    @Test
    @DisplayName("getChallenge: Should throw EntityNotFoundException when not found")
    void getChallenge_NotFound() {
        // given
        var challengeId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        when(challengeRepository.findOneByIdAndUserId(challengeId, userId)).thenReturn(Optional.empty());
        var query = GetChallengeQuery.builder().challengeId(challengeId).userId(userId).build();

        // when
        var exception = assertThrows(EntityNotFoundException.class, () -> challengeService.getChallenge(query));

        // then
        assertEquals("Challenge not found", exception.getMessage());
    }

    @Test
    @DisplayName("submitAttempt: Should handle correct guess")
    void submitAttempt_Correct() {
        // given
        var challengeId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var guess = 10;
        var command = AttemptVerificationCommand.builder().challengeId(challengeId).userId(userId).guess(guess).build();

        var entity = ChallengeEntity.create(List.of(5, 5), OperationType.ADDITION, DifficultyLevel.EASY, userId);
        ReflectionTestUtils.setField(entity, "id", challengeId); // Set ID via reflection
        var createdAt = ZonedDateTime.now();
        ReflectionTestUtils.setField(entity, "createdAt", createdAt); // Use reflection to set createdAt

        entity.updateStatus(ChallengeStatus.PENDING);
        entity.updateCorrectAnswer(10);
        entity.updateExpiresAt(ZonedDateTime.now().plusHours(1));

        when(challengeRepository.findOneByIdAndUserId(challengeId, userId)).thenReturn(Optional.of(entity));
        when(challengeRepository.save(any(ChallengeEntity.class))).thenReturn(entity);

        var expectedResponse = AttemptResponse.builder().challengeId(challengeId).status(ChallengeStatus.CORRECT).correct(true).build();

        // when
        var result = challengeService.submitAttempt(command);

        // then
        assertEquals(expectedResponse, result);
        assertEquals(ChallengeStatus.CORRECT, entity.getStatus());

        var eventCaptor = ArgumentCaptor.forClass(ChallengeSolvedEvent.class);
        verify(challengeAttemptProducer).publishChallengeSolvedMessage(eventCaptor.capture());
        
        var capturedEvent = eventCaptor.getValue();
        assertEquals(challengeId.toString(), capturedEvent.getChallengeAttemptId());
        assertEquals(userId.toString(), capturedEvent.getUserId());
        assertEquals(guess, capturedEvent.getResultAttempt());
        assertTrue(capturedEvent.isCorrect());
        assertNotNull(capturedEvent.getAttemptDate());
    }

    @Test
    @DisplayName("submitAttempt: Should handle incorrect guess")
    void submitAttempt_Incorrect() {
        // given
        var challengeId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var guess = 99; // Wrong
        var command = AttemptVerificationCommand.builder().challengeId(challengeId).userId(userId).guess(guess).build();

        var entity = ChallengeEntity.create(List.of(5, 5), OperationType.ADDITION, DifficultyLevel.EASY, userId);
        ReflectionTestUtils.setField(entity, "id", challengeId); // Set ID via reflection
        var createdAt = ZonedDateTime.now();
        ReflectionTestUtils.setField(entity, "createdAt", createdAt); // Use reflection to set createdAt
        entity.updateStatus(ChallengeStatus.PENDING);
        entity.updateCorrectAnswer(10);
        entity.updateAttemptedAt(ZonedDateTime.now());
        entity.updateExpiresAt(ZonedDateTime.now().plusHours(1));

        when(challengeRepository.findOneByIdAndUserId(challengeId, userId)).thenReturn(Optional.of(entity));
        when(challengeRepository.save(any(ChallengeEntity.class))).thenReturn(entity);

        var expectedResponse = AttemptResponse.builder().challengeId(challengeId).status(ChallengeStatus.INCORRECT).correct(false).build();

        // when
        var result = challengeService.submitAttempt(command);

        // then
        assertEquals(expectedResponse, result);
        assertEquals(ChallengeStatus.INCORRECT, entity.getStatus());

        var eventCaptor = ArgumentCaptor.forClass(ChallengeSolvedEvent.class);
        verify(challengeAttemptProducer).publishChallengeSolvedMessage(eventCaptor.capture());
        var capturedEvent = eventCaptor.getValue();
        assertEquals(challengeId.toString(), capturedEvent.getChallengeAttemptId());
        assertEquals(userId.toString(), capturedEvent.getUserId());
        assertEquals(guess, capturedEvent.getResultAttempt());
        assertFalse(capturedEvent.isCorrect());
        assertNotNull(capturedEvent.getAttemptDate());
    }

    @Test
    @DisplayName("submitAttempt: Should handle expired challenge")
    void submitAttempt_Expired() {
        // given
        var challengeId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var command = AttemptVerificationCommand.builder().challengeId(challengeId).userId(userId).guess(10).build();

        var entity = ChallengeEntity.create(List.of(5, 5), OperationType.ADDITION, DifficultyLevel.EASY, userId);
        ReflectionTestUtils.setField(entity, "id", challengeId); // Set ID via reflection
        var createdAt = ZonedDateTime.now();
        ReflectionTestUtils.setField(entity, "createdAt", createdAt); // Use reflection to set createdAt
        entity.updateStatus(ChallengeStatus.PENDING);
        entity.updateExpiresAt(ZonedDateTime.now().minusMinutes(5));
        entity.updateCorrectAnswer(10);

        when(challengeRepository.findOneByIdAndUserId(challengeId, userId)).thenReturn(Optional.of(entity));
        when(challengeRepository.save(any(ChallengeEntity.class))).thenReturn(entity);

        var expectedResult = AttemptResponse.builder().challengeId(challengeId).status(ChallengeStatus.EXPIRED).build();

        // when
        var result = challengeService.submitAttempt(command);

        // then
        assertEquals(expectedResult, result);
        assertEquals(ChallengeStatus.EXPIRED, entity.getStatus());
    }

    @Test
    @DisplayName("submitAttempt: Should handle status in generated state")
    void submitAttempt_Generated() {
        // given
        var challengeId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var command = AttemptVerificationCommand.builder().challengeId(challengeId).userId(userId).guess(10).build();

        var entity = ChallengeEntity.create(List.of(5, 5), OperationType.ADDITION, DifficultyLevel.EASY, userId);
        ReflectionTestUtils.setField(entity, "id", challengeId); // Set ID via reflection
        var createdAt = ZonedDateTime.now();
        ReflectionTestUtils.setField(entity, "createdAt", createdAt); // Use reflection to set createdAt
        entity.updateStatus(ChallengeStatus.GENERATED);

        when(challengeRepository.findOneByIdAndUserId(challengeId, userId)).thenReturn(Optional.of(entity));

        // when
        var exception = assertThrows(ChallengeDataCorruptedException.class , () -> challengeService.submitAttempt(command));

        // then
        assertEquals("Challenge is not in valid state for verification", exception.getDetail());
        assertEquals(ErrorCode.CONFLICT, exception.getErrorCode());
    }

    @Test
    @DisplayName("submitAttempt: Should throw ChallengeDataCorruptedException if correct answer is missing")
    void submitAttempt_CorruptedData() {
        // given
        var challengeId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var command = AttemptVerificationCommand.builder().challengeId(challengeId).userId(userId).guess(10).build();

        var entity = ChallengeEntity.create(List.of(5, 5), OperationType.ADDITION, DifficultyLevel.EASY, userId);
        ReflectionTestUtils.setField(entity, "id", challengeId); // Set ID via reflection
        var createdAt = ZonedDateTime.now();
        ReflectionTestUtils.setField(entity, "createdAt", createdAt); // Use reflection to set createdAt
        entity.updateStatus(ChallengeStatus.PENDING);
        entity.updateCorrectAnswer(null); // Corrupted

        when(challengeRepository.findOneByIdAndUserId(challengeId, userId)).thenReturn(Optional.of(entity));

        // when
        var exception = assertThrows(ChallengeDataCorruptedException.class, () -> challengeService.submitAttempt(command));

        // then
        assertEquals("Challenge has no correct answer.", exception.getDetail());
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getErrorCode());
    }

    @Test
    @DisplayName("subscribeToChallenge: Should return SseEmitter when challenge exists")
    void subscribeToChallenge_Success() {
        // given
        var challengeId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var command = SubscribeToChallengeCommand.builder()
                .challengeId(challengeId)
                .userId(userId)
                .build();
        var entity = ChallengeEntity.create(List.of(5, 5), OperationType.ADDITION, DifficultyLevel.EASY, userId);
        ReflectionTestUtils.setField(entity, "id", challengeId); // Set ID via reflection
        var createdAt = ZonedDateTime.now();
        ReflectionTestUtils.setField(entity, "createdAt", createdAt); // Use reflection to set createdAt
        entity.updateStatus(ChallengeStatus.PENDING);
        entity.updateCorrectAnswer(10);

        var emitter = new SseEmitter();
        when(challengeRepository.findOneByIdAndUserId(challengeId, userId)).thenReturn(Optional.of(entity));
        when(sseService.subscribe(entity)).thenReturn(emitter);

        // when
        var response = challengeService.subscribeToChallenge(command);

        // then
        assertNotNull(response);
        assertEquals(emitter, response);
    }

    @Test
    @DisplayName("subscribeToChallenge: Should throw ChallengeSubscriptionException when challenge not found")
    void subscribeToChallenge_notFound() {
        // given
        var challengeId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var command = SubscribeToChallengeCommand.builder()
                .challengeId(challengeId)
                .userId(userId)
                .build();
        when(challengeRepository.findOneByIdAndUserId(challengeId, userId)).thenReturn(Optional.empty());

        // when
        var exception = assertThrows(ChallengeSubscriptionException.class, () -> challengeService.subscribeToChallenge(command));

        // then
        assertEquals("Challenge with id " + challengeId + " not found or access denied.", exception.getDetail());
        assertEquals(ErrorCode.CHALLENGE_NOT_FOUND_FOR_SUBSCRIPTION, exception.getErrorCode());
    }
}
