package com.playground.challenge_manager.challenge.dataaccess.repositories;

import com.playground.challenge_manager.challenge.dataaccess.entities.ChallengeAttemptEntity;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers
class ChallengeAttemptRepositoryTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:12");

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ChallengeAttemptRepository challengeAttemptRepository;

    @Test
    void whenSaveAttempt_thenCanBeFoundById() {
        // given
        var attempt = ChallengeAttemptEntity.create(
                UUID.randomUUID(), 10, 20, 200, true, "multiplication", "easy"
        );

        // when
        entityManager.persistAndFlush(attempt);
        var foundAttempt = challengeAttemptRepository.findById(attempt.getId()).orElse(null);

        // then
        assertAll(
                () -> assertNotNull(foundAttempt),
                () -> assertNotNull(foundAttempt.getId()),
                () -> assertEquals(attempt.getUserId(), foundAttempt.getUserId())
        );
    }

    @Test
    void whenFindLast10AttemptsByUser_thenReturnLast10Ordered() {
        // given
        var targetUserId = UUID.randomUUID();
        var otherUserId = UUID.randomUUID();

        // Create 15 attempts for the target user
        IntStream.range(0, 15).forEach(i -> {
            var attempt = ChallengeAttemptEntity.create(
                    targetUserId, 10 + i, 20, 200 + i, true, "addition", "easy"
            );
            // We manually set the date to ensure a clear order
            attempt.setAttemptDate(ZonedDateTime.now().minusMinutes(i));
            entityManager.persist(attempt);
        });

        // Create 5 attempts for another user (these should be ignored)
        IntStream.range(0, 5).forEach(i -> {
            var attempt = ChallengeAttemptEntity.create(
                    otherUserId, 10, 20, 200, true, "multiplication", "easy"
            );
            entityManager.persist(attempt);
        });
        entityManager.flush();

        // when
        var foundAttempts = challengeAttemptRepository.findLast10AttemptsByUser(targetUserId);

        // then
        assertAll(
                // 1. Check that it returns exactly 10 results
                () -> assertEquals(10, foundAttempts.size()),
                // 2. Check that all returned attempts belong to the correct user
                () -> assertTrue(foundAttempts.stream().allMatch(a -> a.getUserId().equals(targetUserId))),
                // 3. Check that the list is sorted by date descending
                () -> {
                    var sortedList = foundAttempts.stream().sorted(Comparator.comparing(ChallengeAttemptEntity::getAttemptDate).reversed()).toList();
                    assertEquals(sortedList, foundAttempts, "The list should be ordered by attemptDate descending.");
                }
        );
    }

    // --- Bean Validation Tests ---

    @Test
    void whenSaveAttemptWithBlankGame_thenThrowException() {
        // given
        var attempt = ChallengeAttemptEntity.create(
                UUID.randomUUID(), 10, 20, 200, true, "", "easy"
        );

        // when & then
        assertThrows(ConstraintViolationException.class, () -> entityManager.persistAndFlush(attempt));
    }

    @Test
    void whenSaveAttemptWithBlankDifficulty_thenThrowException() {
        // given
        var attempt = ChallengeAttemptEntity.create(
                UUID.randomUUID(), 10, 20, 200, true, "addition", ""
        );

        // when & then
        assertThrows(ConstraintViolationException.class, () -> entityManager.persistAndFlush(attempt));
    }
}
