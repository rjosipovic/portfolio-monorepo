package com.playground.gamification_manager.game.dataaccess.repositories;

import com.playground.gamification_manager.game.dataaccess.domain.ScoreEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Testcontainers
class ScoreRepositoryTest {

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
    private ScoreRepository scoreRepository;

    @Test
    void whenSaveScore_thenCanBeFoundById() {
        // given
        var score = ScoreEntity.create(UUID.randomUUID(), UUID.randomUUID(), 100);

        // when
        entityManager.persistAndFlush(score);
        var foundScore = scoreRepository.findById(score.getId()).orElse(null);

        // then
        assertAll(
                () -> assertNotNull(foundScore),
                () -> assertNotNull(foundScore.getId()),
                () -> assertEquals(score.getUserId(), foundScore.getUserId()),
                () -> assertEquals(100, foundScore.getScore())
        );
    }

    @Test
    void whenTotalScoreByUserId_withExistingScores_thenReturnCorrectSum() {
        // given
        var targetUserId = UUID.randomUUID();
        var otherUserId = UUID.randomUUID();

        // Scores for the target user
        entityManager.persist(ScoreEntity.create(targetUserId, UUID.randomUUID(), 100));
        entityManager.persist(ScoreEntity.create(targetUserId, UUID.randomUUID(), 50));

        // Score for another user (should be ignored)
        entityManager.persist(ScoreEntity.create(otherUserId, UUID.randomUUID(), 200));
        entityManager.flush();

        // when
        var totalScore = scoreRepository.totalScoreByUserId(targetUserId);

        // then
        assertEquals(150, totalScore);
    }

    @Test
    void whenTotalScorePerUser_withMultipleUsers_thenReturnScoresOrderedDescending() {
        // given
        var userA = UUID.randomUUID(); // Total: 150
        var userB = UUID.randomUUID(); // Total: 200
        var userC = UUID.randomUUID(); // Total: 75

        entityManager.persist(ScoreEntity.create(userA, UUID.randomUUID(), 100));
        entityManager.persist(ScoreEntity.create(userA, UUID.randomUUID(), 50));
        entityManager.persist(ScoreEntity.create(userB, UUID.randomUUID(), 200));
        entityManager.persist(ScoreEntity.create(userC, UUID.randomUUID(), 75));
        entityManager.flush();

        // when
        var leaderboard = scoreRepository.totalScorePerUser();

        // then
        assertAll(
                () -> assertEquals(3, leaderboard.size()),
                // Check the order and scores
                () -> assertEquals(userB, leaderboard.get(0).getUserId()),
                () -> assertEquals(200, leaderboard.get(0).getTotalScore()),
                () -> assertEquals(userA, leaderboard.get(1).getUserId()),
                () -> assertEquals(150, leaderboard.get(1).getTotalScore()),
                () -> assertEquals(userC, leaderboard.get(2).getUserId()),
                () -> assertEquals(75, leaderboard.get(2).getTotalScore())
        );
    }
}
