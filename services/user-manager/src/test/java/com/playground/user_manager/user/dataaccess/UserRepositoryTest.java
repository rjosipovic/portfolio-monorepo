package com.playground.user_manager.user.dataaccess;

import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers
@TestPropertySource(properties = {
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
        "spring.cloud.consul.config.enabled=false",
        "spring.cloud.consul.discovery.enabled=false"
})
class UserRepositoryTest {

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
    private UserRepository userRepository;

    @Test
    void whenSaveUser_thenCanBeFoundById() {
        // given
        var user = UserEntity.create("test-alias", "test@email.com", null, null);

        // when
        entityManager.persistAndFlush(user);
        var foundUser = userRepository.findById(user.getId()).orElse(null);

        // then
        assertAll(
                () -> assertNotNull(foundUser),
                () -> assertNotNull(foundUser.getAlias()),
                () -> assertEquals(user.getAlias(), foundUser.getAlias())
        );
    }

    @Test
    void whenFindByExistingAlias_thenReturnUser() {
        // given
        var alias = "test-alias";
        var user = UserEntity.create(alias, "test@email.com", null, null);
        entityManager.persistAndFlush(user);

        // when
        var foundUser = userRepository.findByAlias(alias);

        // then
        assertAll(
                () -> assertTrue(foundUser.isPresent()),
                () -> assertEquals(alias, foundUser.get().getAlias())
        );
    }

    @Test
    void whenFindByNonExistingAlias_thenReturnEmpty() {
        // given
        var nonExistingAlias = "non-existing-alias";

        // when
        var foundUser = userRepository.findByAlias(nonExistingAlias);

        // then
        assertFalse(foundUser.isPresent());
    }

    @Test
    void whenFindByExistingEmail_thenReturnUser() {
        // given
        var email = "test@email.com";
        var user = UserEntity.create("test-alias", email, null, null);
        entityManager.persistAndFlush(user);

        // when
        var foundUser = userRepository.findByEmail(email);

        // then
        assertAll(
                () -> assertTrue(foundUser.isPresent()),
                () -> assertEquals(email, foundUser.get().getEmail())
        );
    }

    @Test
    void whenFindByNonExistingEmail_thenReturnEmpty() {
        // given
        var nonExistingEmail = "non-existing@email.com";

        // when
        var foundUser = userRepository.findByEmail(nonExistingEmail);

        // then
        assertFalse(foundUser.isPresent());
    }

    @Test
    void whenFindByIdIn_withExistingIds_thenReturnUsers() {
        // given
        var user1 = UserEntity.create("alias1", "email1@test.com", null, null);
        var user2 = UserEntity.create("alias2", "email2@test.com", null, null);
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        var idsToFind = List.of(user1.getId(), user2.getId());

        // when
        var foundUsers = userRepository.findByIdIn(idsToFind);

        // then
        assertAll(
                () -> assertEquals(2, foundUsers.size()),
                () -> assertTrue(foundUsers.stream().anyMatch(u -> u.getId().equals(user1.getId()))),
                () -> assertTrue(foundUsers.stream().anyMatch(u -> u.getId().equals(user2.getId())))
        );
    }

    @Test
    void whenFindByIdIn_withMixedIds_thenReturnOnlyFoundUsers() {
        // given
        var user1 = UserEntity.create("alias1", "email1@test.com", null, null);
        entityManager.persistAndFlush(user1);

        var nonExistingId = UUID.randomUUID();
        var idsToFind = List.of(user1.getId(), nonExistingId);

        // when
        var foundUsers = userRepository.findByIdIn(idsToFind);

        // then
        assertAll(
                () -> assertEquals(1, foundUsers.size()),
                () -> assertEquals(user1.getId(), foundUsers.get(0).getId())
        );
    }

    @Test
    void whenFindByIdIn_withEmptyList_thenReturnEmptyList() {
        // given
        var emptyList = List.<UUID>of();

        // when
        var foundUsers = userRepository.findByIdIn(emptyList);

        // then
        assertTrue(foundUsers.isEmpty());
    }

    // --- Database Constraint Tests ---

    @Test
    void whenSaveUserWithDuplicateAlias_thenThrowException() {
        // given
        var user1 = UserEntity.create("duplicate-alias", "email1@test.com", null, null);
        entityManager.persistAndFlush(user1);

        var user2 = UserEntity.create("duplicate-alias", "email2@test.com", null, null);

        // when & then
        assertThrows(PersistenceException.class, () -> entityManager.persistAndFlush(user2));
    }

    @Test
    void whenSaveUserWithDuplicateEmail_thenThrowException() {
        // given
        var user1 = UserEntity.create("alias1", "duplicate@email.com", null, null);
        entityManager.persistAndFlush(user1);

        var user2 = UserEntity.create("alias2", "duplicate@email.com", null, null);

        // when & then
        assertThrows(PersistenceException.class, () -> entityManager.persistAndFlush(user2));
    }

    // --- Bean Validation Tests ---

    @Test
    void whenSaveUserWithInvalidEmail_thenThrowException() {
        // given
        var user = UserEntity.create("test-alias", "not-an-email", null, null);

        // when & then
        assertThrows(ConstraintViolationException.class, () -> entityManager.persistAndFlush(user));
    }

    @Test
    void whenSaveUserWithFutureBirthdate_thenThrowException() {
        // given
        var futureDate = LocalDate.now().plusDays(1);
        var user = UserEntity.create("test-alias", "test@email.com", futureDate, null);

        // when & then
        assertThrows(ConstraintViolationException.class, () -> entityManager.persistAndFlush(user));
    }

    @Test
    void whenSaveUserWithInvalidGender_thenThrowException() {
        // given
        var user = UserEntity.create("test-alias", "test@email.com", null, "other");

        // when & then
        assertThrows(ConstraintViolationException.class, () -> entityManager.persistAndFlush(user));
    }
}
