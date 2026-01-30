package com.playground.challenge_manager.challenge.dataaccess.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playground.challenge_manager.challenge.dataaccess.entities.ChallengeEntity;
import com.playground.challenge_manager.challenge.services.model.ChallengeStatus;
import com.playground.challenge_manager.challenge.services.model.DifficultyLevel;
import com.playground.challenge_manager.challenge.services.model.OperationType;
import com.playground.challenge_manager.config.StaticContextAccessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers
@Import(StaticContextAccessor.class) // Import the accessor so it's part of the context
@TestPropertySource(properties = {
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
        "spring.cloud.consul.config.enabled=false",
        "spring.cloud.consul.discovery.enabled=false"
})
class ChallengeRepositoryTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:12");

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    // We need to provide an ObjectMapper because @DataJpaTest doesn't provide one for injection
    @TestConfiguration
    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Autowired
    private ChallengeRepository challengeRepository;

    @Test
    void shouldSaveChallenge() {
        // given
        var userId = UUID.randomUUID();
        var operands = List.of(1, 2);
        var operationType = OperationType.ADDITION;
        var difficulty = DifficultyLevel.EASY;
        var challengeEntity = ChallengeEntity.create(operands, operationType, difficulty, userId);
        //challengeEntity.setCreatedAt(ZonedDateTime.now()); // Manually set the timestamps that Hibernate would normally handle
        challengeEntity.updateExpiresAt(ZonedDateTime.now().plusMinutes(5));
        // when
        var saved = challengeRepository.saveAndFlush(challengeEntity);
        // then
        assertAll(
                () -> assertNotNull(saved),
                () -> assertNotNull(saved.getId()),
                () -> assertNotNull(saved.getCreatedAt()),
                () -> assertNotNull(saved.getOperationType()),
                () -> assertNotNull(saved.getOperands()),
                () -> assertNotNull(saved.getStatus()),
                () -> assertEquals(operands, saved.getOperands()),
                () -> assertEquals(operationType, saved.getOperationType())
        );
    }

    @Test
    void shouldFindChallengeById() {
        // given
        var userId = UUID.randomUUID();
        var operands = List.of(10, 20);
        var operationType = OperationType.ADDITION;
        var difficulty = DifficultyLevel.EASY;
        var challengeToSave = ChallengeEntity.create(operands, operationType, difficulty, userId);
        //challengeToSave.setCreatedAt(ZonedDateTime.now());
        challengeToSave.updateExpiresAt(ZonedDateTime.now().plusMinutes(5));

        // Save the entity first to get a persisted ID
        var savedChallenge = challengeRepository.saveAndFlush(challengeToSave);
        assertNotNull(savedChallenge.getId());

        // when
        var foundChallengeOptional = challengeRepository.findById(savedChallenge.getId());

        // then
        assertTrue(foundChallengeOptional.isPresent());
        var foundChallenge = foundChallengeOptional.get();

        assertAll(
                () -> assertEquals(savedChallenge.getId(), foundChallenge.getId()),
                () -> assertEquals(operationType, foundChallenge.getOperationType()),
                () -> assertEquals(operands, foundChallenge.getOperands())
        );
    }

    @Test
    void shouldUpdateChallenge() {
        // given
        var userId = UUID.randomUUID();
        var operands = List.of(30, 5);
        var operationType = OperationType.SUBTRACTION;
        var difficulty = DifficultyLevel.EASY;
        var challengeToSave = ChallengeEntity.create(operands, operationType, difficulty, userId);
        //challengeToSave.setCreatedAt(ZonedDateTime.now());
        challengeToSave.updateExpiresAt(ZonedDateTime.now().plusMinutes(5));

        var savedChallenge = challengeRepository.saveAndFlush(challengeToSave);
        assertEquals(ChallengeStatus.GENERATED, savedChallenge.getStatus()); // Verify initial state
        assertNull(savedChallenge.getCorrectAnswer());

        // when
        // Simulate the async worker updating the entity
        savedChallenge.updateStatus(ChallengeStatus.PENDING);
        savedChallenge.updateCorrectAnswer(25);
        var updatedChallenge = challengeRepository.saveAndFlush(savedChallenge);

        // then
        assertAll(
                () -> assertNotNull(updatedChallenge),
                () -> assertEquals(savedChallenge.getId(), updatedChallenge.getId()),
                () -> assertEquals(ChallengeStatus.PENDING, updatedChallenge.getStatus()),
                () -> assertEquals(25, updatedChallenge.getCorrectAnswer())
        );
    }

    @Test
    void shouldDeleteChallenge() {
        // given
        var userId = UUID.randomUUID();
        var operands = List.of(7, 8);
        var operationType = OperationType.MULTIPLICATION;
        var difficulty = DifficultyLevel.EASY;
        var challengeToSave = ChallengeEntity.create(operands, operationType, difficulty, userId);
        //challengeToSave.setCreatedAt(ZonedDateTime.now());
        challengeToSave.updateExpiresAt(ZonedDateTime.now().plusMinutes(5));

        var savedChallenge = challengeRepository.saveAndFlush(challengeToSave);
        var challengeId = savedChallenge.getId();
        assertTrue(challengeRepository.existsById(challengeId));

        // when
        challengeRepository.deleteById(challengeId);
        challengeRepository.flush();

        // then
        assertFalse(challengeRepository.existsById(challengeId));
    }

    @Test
    void shouldFindChallengeByIdAndUserId() {
        // given
        var userId = UUID.randomUUID();
        var otherUserId = UUID.randomUUID();
        var operands = List.of(10, 20);
        var operationType = OperationType.ADDITION;
        var difficulty = DifficultyLevel.EASY;
        var challengeToSave = ChallengeEntity.create(operands, operationType, difficulty, userId);
        //challengeToSave.setCreatedAt(ZonedDateTime.now());
        challengeToSave.updateExpiresAt(ZonedDateTime.now().plusMinutes(5));

        var savedChallenge = challengeRepository.saveAndFlush(challengeToSave);
        var challengeId = savedChallenge.getId();

        // when
        var foundChallenge = challengeRepository.findOneByIdAndUserId(challengeId, userId);
        var notFoundChallenge = challengeRepository.findOneByIdAndUserId(challengeId, otherUserId);

        // then
        assertTrue(foundChallenge.isPresent());
        assertEquals(savedChallenge.getId(), foundChallenge.get().getId());
        assertEquals(userId, foundChallenge.get().getUserId());

        assertFalse(notFoundChallenge.isPresent());
    }

    @Test
    void shouldNotFindChallenge_whenUserIdDoesNotMatch() {
        // given
        var ownerId = UUID.randomUUID();
        var otherUserId = UUID.randomUUID();
        var operands = List.of(5, 5);
        var challenge = ChallengeEntity.create(operands, OperationType.ADDITION, DifficultyLevel.EASY, ownerId);
        //challenge.setCreatedAt(ZonedDateTime.now());
        challenge.updateExpiresAt(ZonedDateTime.now().plusMinutes(5));

        var savedChallenge = challengeRepository.saveAndFlush(challenge);

        // when
        var result = challengeRepository.findOneByIdAndUserId(savedChallenge.getId(), otherUserId);

        // then
        assertFalse(result.isPresent());
    }
}
