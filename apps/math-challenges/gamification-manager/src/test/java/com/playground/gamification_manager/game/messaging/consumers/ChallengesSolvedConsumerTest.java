package com.playground.gamification_manager.game.messaging.consumers;

import com.playground.gamification_manager.game.messaging.events.ChallengeSolvedEvent;
import com.playground.gamification_manager.game.service.interfaces.GameService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChallengesSolvedConsumerTest {

    @Mock
    private GameService gameService;
    @InjectMocks
    private ChallengesSolvedConsumer consumer;

    @Test
    void handleChallengeSolved_shouldDelegateToGameService() {
        // given
        var event = mock(ChallengeSolvedEvent.class);

        // when
        consumer.handleChallengeSolved(event);

        // then
        verify(gameService).process(event);
    }
}