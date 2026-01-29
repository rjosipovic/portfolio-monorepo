package com.playground.challenge_manager.challenge.services.impl;

import com.playground.challenge_manager.challenge.dataaccess.entities.ChallengeEntity;
import com.playground.challenge_manager.challenge.dataaccess.repositories.ChallengeRepository;
import com.playground.challenge_manager.challenge.mappers.ChallengeMapper;
import com.playground.challenge_manager.challenge.messaging.events.ChallengeReadyEvent;
import com.playground.challenge_manager.challenge.messaging.producers.ChallengeReadyEventProducer;
import com.playground.challenge_manager.challenge.services.impl.calculation.CalculatorFactory;
import com.playground.challenge_manager.challenge.services.interfaces.OperationCalculator;
import com.playground.challenge_manager.challenge.services.model.ChallengeStatus;
import com.playground.challenge_manager.challenge.services.model.DifficultyLevel;
import com.playground.challenge_manager.challenge.services.model.OperationType;
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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DefaultChallengeProcessor Test")
class DefaultChallengeProcessorTest {

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private CalculatorFactory calculatorFactory;

    @Mock
    private OperationCalculator calculator;

    @Spy
    private ChallengeMapper challengeMapper = Mappers.getMapper(ChallengeMapper.class);

    @Mock
    private ChallengeReadyEventProducer challengeReadyEventProducer;


    @InjectMocks
    private DefaultChallengeProcessor challengeProcessor;

    @Test
    @DisplayName("process: Should calculate answer and update status to PENDING")
    void process_Success() {
        // given
        var challengeId = UUID.randomUUID();
        var operands = List.of(10, 20);
        var operation = OperationType.ADDITION;
        var expectedAnswer = 30;

        var challengeEntity = ChallengeEntity.create(operands, operation, DifficultyLevel.EASY, UUID.randomUUID()); // sets status to GENERATED
        ReflectionTestUtils.setField(challengeEntity, "id", challengeId);
        var createdAt = ZonedDateTime.now();
        ReflectionTestUtils.setField(challengeEntity, "createdAt", createdAt);

        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challengeEntity));
        when(calculatorFactory.getCalculator(operation)).thenReturn(calculator);
        when(calculator.calculate(operands)).thenReturn(expectedAnswer);

        // when
        challengeProcessor.process(challengeId);

        // then
        assertEquals(ChallengeStatus.PENDING, challengeEntity.getStatus());
        assertEquals(expectedAnswer, challengeEntity.getCorrectAnswer());
        verify(challengeRepository).save(challengeEntity);

        var captor = ArgumentCaptor.forClass(ChallengeReadyEvent.class);
        verify(challengeReadyEventProducer).publishChallengeReadyEvent(captor.capture());
        var capturedEvent = captor.getValue();
        assertEquals(challengeId, capturedEvent.getChallenge().getId());
        assertEquals(ChallengeStatus.PENDING, capturedEvent.getChallenge().getStatus());
    }

    @Test
    @DisplayName("process: Should throw EntityNotFoundException if challenge not found")
    void process_NotFound() {
        // given
        var challengeId = UUID.randomUUID();
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.empty());

        // when
        var exception = assertThrows(EntityNotFoundException.class, () -> challengeProcessor.process(challengeId));

        // then
        assertEquals("Challenge not found" + challengeId, exception.getMessage());
        verify(challengeRepository, never()).save(any());
        verify(calculatorFactory, never()).getCalculator(any());
        verify(calculator, never()).calculate(any());
        verify(challengeReadyEventProducer, never()).publishChallengeReadyEvent(any());
    }

    @Test
    @DisplayName("process: Should do nothing if status is not GENERATED")
    void process_InvalidState() {
        // given
        var challengeId = UUID.randomUUID();
        var challengeEntity = ChallengeEntity.create(List.of(1, 2), OperationType.ADDITION, DifficultyLevel.EASY, UUID.randomUUID());
        challengeEntity.updateStatus(ChallengeStatus.PENDING); // Already processed

        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challengeEntity));

        // when
        challengeProcessor.process(challengeId);

        // then
        // Should NOT calculate or save
        verify(calculatorFactory, never()).getCalculator(any());
        verify(calculator, never()).calculate(any());
        verify(challengeRepository, never()).save(any());
        verify(challengeReadyEventProducer, never()).publishChallengeReadyEvent(any());
    }
}
