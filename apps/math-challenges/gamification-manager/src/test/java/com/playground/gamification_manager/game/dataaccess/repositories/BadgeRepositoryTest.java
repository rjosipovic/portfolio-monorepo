package com.playground.gamification_manager.game.dataaccess.repositories;

import com.playground.gamification_manager.game.dataaccess.domain.BadgeEntity;
import com.playground.gamification_manager.game.dataaccess.domain.BadgeType;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers
class BadgeRepositoryTest {

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
    private BadgeRepository badgeRepository;

    @Test
    void whenSaveBadge_thenCanBeFoundById() {
        // given
        var badge = BadgeEntity.create(UUID.randomUUID(), BadgeType.BRONZE);

        // when
        entityManager.persistAndFlush(badge);
        var foundBadge = badgeRepository.findById(badge.getId()).orElse(null);

        // then
        assertAll(
                () -> assertNotNull(foundBadge),
                () -> assertNotNull(foundBadge.getId()),
                () -> assertEquals(badge.getUserId(), foundBadge.getUserId()),
                () -> assertEquals(BadgeType.BRONZE, foundBadge.getBadgeType())
        );
    }

    @Test
    void whenFindAllByUserId_withExistingBadges_thenReturnBadgesForUser() {
        // given
        var targetUserId = UUID.randomUUID();
        var otherUserId = UUID.randomUUID();

        // Badges for the target user
        entityManager.persist(BadgeEntity.create(targetUserId, BadgeType.BRONZE));
        entityManager.persist(BadgeEntity.create(targetUserId, BadgeType.SILVER));

        // Badge for another user (should be ignored)
        entityManager.persist(BadgeEntity.create(otherUserId, BadgeType.GOLD));
        entityManager.flush();

        // when
        var foundBadges = badgeRepository.findAllByUserId(targetUserId);

        // then
        assertAll(
                () -> assertEquals(2, foundBadges.size()),
                () -> assertTrue(foundBadges.stream().allMatch(b -> b.getUserId().equals(targetUserId)))
        );
    }
}
