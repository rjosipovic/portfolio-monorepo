package com.playground.user_manager.user.dataaccess;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UserEntityTest {

    @Test
    void whenCreateUserWithNullAlias_thenThrowException() {
        // This is a unit test for the UserEntity factory method's validation.
        assertThrows(IllegalArgumentException.class, () -> UserEntity.create(null, "test@email.com", null, null));
    }

    @Test
    void whenCreateUserWithNullEmail_thenThrowException() {
        // This is a unit test for the UserEntity factory method's validation.
        assertThrows(IllegalArgumentException.class, () -> UserEntity.create("test-alias", null, null, null));
    }
}
