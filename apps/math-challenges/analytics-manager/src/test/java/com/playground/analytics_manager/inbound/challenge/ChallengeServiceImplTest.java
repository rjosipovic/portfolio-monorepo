package com.playground.analytics_manager.inbound.challenge;

import com.playground.analytics_manager.dataaccess.entity.ChallengeEntity;
import com.playground.analytics_manager.dataaccess.entity.UserEntity;
import com.playground.analytics_manager.dataaccess.repository.ChallengeRepository;
import com.playground.analytics_manager.dataaccess.repository.UserRepository;
import com.playground.analytics_manager.inbound.messaging.events.ChallengeSolvedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChallengeServiceImplTest {

    @Mock
    private ChallengeRepository challengeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @InjectMocks
    private ChallengeServiceImpl challengeService;

    @Test
    void process_shouldSaveChallengeEntityWithCorrectFields() {
        //given
        var userId = UUID.randomUUID();
        var alias = "alias";
        var challengeAttemptId = UUID.randomUUID();
        var userEntity = UserEntity.create(userId, alias);
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        var event = new ChallengeSolvedEvent(
                userId.toString(),
                challengeAttemptId.toString(),
                12,
                34,
                46,
                true,
                "addition",
                "easy",
                ZonedDateTime.now()
        );

        //when
        challengeService.process(event);

        //then
        var captor = ArgumentCaptor.forClass(ChallengeEntity.class);
        verify(challengeRepository).save(captor.capture());
        verify(applicationEventPublisher).publishEvent(event);
        var saved = captor.getValue();

        assertAll(
                () -> assertNotNull(saved.getUserAttempt()),
                () -> assertEquals(challengeAttemptId, saved.getId()),
                () -> assertEquals(12, saved.getFirstNumber()),
                () -> assertEquals(34, saved.getSecondNumber()),
                () -> assertEquals("addition", saved.getGame()),
                () -> assertEquals("easy", saved.getDifficulty()),
                () -> assertEquals(event.getAttemptDate(), saved.getUserAttempt().getAttemptDate()),
                () -> assertEquals(userEntity, saved.getUserAttempt().getUser()),
                () -> assertEquals(true, saved.getUserAttempt().getCorrect())
        );
    }

    @Test
    void process_shouldDoNothingIfUserNotFound() {
        //given
        var userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        var event = new ChallengeSolvedEvent(
                userId.toString(),
                UUID.randomUUID().toString(),
                12,
                34,
                46,
                true,
                "addition",
                "easy",
                ZonedDateTime.now()
        );

        //when
        challengeService.process(event);

        //then
        verify(challengeRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }
}