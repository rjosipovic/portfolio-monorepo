package com.playground.gamification_manager.game.service;


import com.playground.gamification_manager.game.messaging.events.ChallengeSolvedEvent;
import com.playground.gamification_manager.game.service.impl.GameServiceImpl;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.ChallengeSolvedChain;
import com.playground.gamification_manager.game.service.impl.challengesolved.chain.ChallengeSolvedContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GameServiceImplTest {

    @Mock
    private ChallengeSolvedChain challengeSolvedChain;

    @InjectMocks
    private GameServiceImpl gameService;

    @Test
    void shouldProcessSolvedAttempt() {
        //given
        var userId = UUID.randomUUID().toString();
        var challengeAttemptId = UUID.randomUUID().toString();
        var firstNumber = 1;
        var secondNumber = 2;
        var correct = true;
        var game = "multiplication";
        var challengeSolved = ChallengeSolvedEvent.builder()
                .userId(userId)
                .challengeAttemptId(challengeAttemptId)
                .firstNumber(firstNumber)
                .secondNumber(secondNumber)
                .correct(correct)
                .game(game)
                .build();
        var ctx = new ChallengeSolvedContext(challengeSolved);

        //when
        gameService.process(challengeSolved);

        //then
        verify(challengeSolvedChain).handle(ctx);
    }
}