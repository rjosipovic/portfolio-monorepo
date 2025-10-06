package com.playground.analytics_manager.dataaccess.repository;

import com.playground.analytics_manager.dataaccess.entity.ChallengeEntity;
import com.playground.analytics_manager.dataaccess.entity.UserAttempt;
import com.playground.analytics_manager.dataaccess.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataNeo4jTest
@Testcontainers
class ChallengeRepositoryTest {

    @Container
    static final Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:4.4");

    @DynamicPropertySource
    static void configureNeo4j(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", neo4j::getAdminPassword);
    }

    @Autowired
    private Neo4jTemplate neo4jTemplate;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Test
    void whenSaveChallenge_thenCanBeFoundById() {
        // given
        var userId = UUID.randomUUID();
        var user = UserEntity.create(userId, "test-alias");
        neo4jTemplate.save(user);

        var userAttempt = UserAttempt.create(ZonedDateTime.now(), 3500, true, user);
        var challenge = ChallengeEntity.create(
                UUID.randomUUID(), 50, 70, "multiplication", "easy", userAttempt
        );

        // when
        challengeRepository.save(challenge);
        var foundChallenge = challengeRepository.findById(challenge.getId()).orElse(null);

        // then
        assertAll(
                () -> assertNotNull(foundChallenge),
                () -> assertNotNull(foundChallenge.getId()),
                () -> assertEquals(challenge.getGame(), foundChallenge.getGame()),
                () -> assertNotNull(foundChallenge.getUserAttempt()),
                () -> assertEquals(userId, foundChallenge.getUserAttempt().getUser().getId())
        );
    }

    @Test
    void whenFindByUserAttempt_UserId_withExistingChallenges_thenReturnChallengesForUser() {
        // given
        var targetUserId = UUID.randomUUID();
        var targetUser = UserEntity.create(targetUserId, "target-user");
        neo4jTemplate.save(targetUser);

        var otherUserId = UUID.randomUUID();
        var otherUser = UserEntity.create(otherUserId, "other-user");
        neo4jTemplate.save(otherUser);

        // Challenge for the target user
        var attempt1 = UserAttempt.create(ZonedDateTime.now(), 100, true, targetUser);
        var challenge1 = ChallengeEntity.create(UUID.randomUUID(), 10, 10, "addition", "easy", attempt1);
        challengeRepository.save(challenge1);

        // Challenge for the other user (should be ignored)
        var attempt2 = UserAttempt.create(ZonedDateTime.now(), 200, false, otherUser);
        var challenge2 = ChallengeEntity.create(UUID.randomUUID(), 20, 10, "subtraction", "medium", attempt2);
        challengeRepository.save(challenge2);

        // when
        List<ChallengeEntity> foundChallenges = challengeRepository.findByUserAttempt_UserId(targetUserId);

        // then
        assertAll(
                () -> assertEquals(1, foundChallenges.size()),
                () -> assertEquals(challenge1.getId(), foundChallenges.get(0).getId()),
                () -> assertEquals(targetUserId, foundChallenges.get(0).getUserAttempt().getUser().getId())
        );
    }

    @Test
    void whenFindByUserAttempt_UserId_withUserButNoAttempts_thenReturnEmptyList() {
        // given
        var targetUserId = UUID.randomUUID();
        var targetUser = UserEntity.create(targetUserId, "target-user");
        neo4jTemplate.save(targetUser);

        // when
        List<ChallengeEntity> foundChallenges = challengeRepository.findByUserAttempt_UserId(targetUserId);

        // then
        assertTrue(foundChallenges.isEmpty());
    }

    @Test
    void whenExistsWithMatchingIds_thenReturnTrue() {
        // given
        var userId = UUID.randomUUID();
        var user = UserEntity.create(userId, "test-alias");
        neo4jTemplate.save(user);

        var userAttempt = UserAttempt.create(ZonedDateTime.now(), 3500, true, user);
        var challenge = ChallengeEntity.create(
                UUID.randomUUID(), 50, 70, "multiplication", "easy", userAttempt
        );
        challengeRepository.save(challenge);

        // when
        boolean exists = challengeRepository.existsByIdAndUserAttempt_User_Id(challenge.getId(), userId);

        // then
        assertTrue(exists);
    }

    @Test
    void whenExistsWithNonMatchingIds_thenReturnFalse() {
        // given
        var userId = UUID.randomUUID();
        var user = UserEntity.create(userId, "test-alias");
        neo4jTemplate.save(user);

        var userAttempt = UserAttempt.create(ZonedDateTime.now(), 3500, true, user);
        var challenge = ChallengeEntity.create(
                UUID.randomUUID(), 50, 70, "multiplication", "easy", userAttempt
        );
        challengeRepository.save(challenge);

        var nonExistentUserId = UUID.randomUUID();

        // when
        boolean exists = challengeRepository.existsByIdAndUserAttempt_User_Id(challenge.getId(), nonExistentUserId);

        // then
        assertFalse(exists);
    }
}
