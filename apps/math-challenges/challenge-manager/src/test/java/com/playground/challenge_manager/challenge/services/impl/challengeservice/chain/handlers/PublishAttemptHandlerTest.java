package com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.handlers;

import com.playground.challenge_manager.challenge.mappers.ChallengeMapper;
import com.playground.challenge_manager.challenge.messaging.events.ChallengeSolvedEvent;
import com.playground.challenge_manager.challenge.messaging.producers.ChallengeSolvedProducer;
import com.playground.challenge_manager.challenge.services.impl.challengeservice.chain.AttemptVerifierContext;
import com.playground.challenge_manager.challenge.services.model.ChallengeAttempt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublishAttemptHandlerTest {

    @Mock
    private ChallengeSolvedProducer challengeSolvedProducer;

    private final ChallengeMapper challengeMapper = Mappers.getMapper(ChallengeMapper.class);
    private PublishAttemptHandler handler;

    @BeforeEach
    void init() {
        handler = new PublishAttemptHandler(challengeSolvedProducer, challengeMapper);
    }

    @Test
    void shouldPublishChallengeSolvedEvent() {
        // given
        var userId = UUID.randomUUID();
        var attemptId = UUID.randomUUID();
        var firstNumber = 3;
        var secondNumber = 4;
        var resultAttempt = 7;
        var isCorrect = true;
        var game = "addition";
        var difficulty = "easy";

        // Mock AttemptVerifierContext and ChallengeAttempt
        var challengeAttempt = mock(ChallengeAttempt.class);
        when(challengeAttempt.getUserId()).thenReturn(userId);
        when(challengeAttempt.getChallengeAttemptId()).thenReturn(attemptId);
        when(challengeAttempt.getFirstNumber()).thenReturn(firstNumber);
        when(challengeAttempt.getSecondNumber()).thenReturn(secondNumber);
        when(challengeAttempt.getResultAttempt()).thenReturn(resultAttempt);
        when(challengeAttempt.getCorrect()).thenReturn(isCorrect);
        when(challengeAttempt.getGame()).thenReturn(game);
        when(challengeAttempt.getDifficulty()).thenReturn(difficulty);

        var ctx = mock(AttemptVerifierContext.class);
        when(ctx.getChallengeAttempt()).thenReturn(challengeAttempt);

        // when
        handler.handle(ctx);

        // then
        var expectedEvent = ChallengeSolvedEvent.builder()
                .userId(userId.toString())
                .challengeAttemptId(attemptId.toString())
                .firstNumber(firstNumber)
                .secondNumber(secondNumber)
                .resultAttempt(resultAttempt)
                .correct(isCorrect)
                .game(game)
                .difficulty(difficulty)
                .build();
        verify(challengeSolvedProducer).publishChallengeSolvedMessage(expectedEvent);
    }
}