package com.playground.analytics_manager.inbound.challenge;

import com.playground.analytics_manager.dataaccess.entity.UserEntity;
import com.playground.analytics_manager.dataaccess.repository.ChallengeRepository;
import com.playground.analytics_manager.dataaccess.repository.UserRepository;
import com.playground.analytics_manager.inbound.messaging.events.ChallengeSolvedEvent;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChallengeServiceImplTest {

    @Mock
    private ChallengeRepository challengeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private Validator validator;

    private ChallengeServiceImpl challengeService;

    @BeforeEach
    void setUp() {
        var factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        challengeService = new ChallengeServiceImpl(challengeRepository, userRepository, applicationEventPublisher, validator);
    }

    @Test
    void whenProcessEventWithInvalidData_thenThrowException() {
        // given
        // This event is invalid because the 'game' field is blank
        var userId = UUID.randomUUID();
        var event = ChallengeSolvedEvent.builder()
                .userId(userId.toString())
                .challengeAttemptId(UUID.randomUUID().toString())
                .firstNumber(10)
                .secondNumber(200)
                .resultAttempt(210)
                .correct(true)
                .game("")
                .difficulty("easy")
                .attemptDate(ZonedDateTime.now())
                .build();

        var userEntity = UserEntity.create(userId, "test-alias");
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(userEntity));

        // when & then
        // Assert that processing this event throws a ConstraintViolationException
        // because the resulting ChallengeEntity will fail bean validation.
        assertThrows(ConstraintViolationException.class, () -> challengeService.process(event));
    }
}
