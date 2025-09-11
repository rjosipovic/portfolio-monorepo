package com.playground.user_manager.user;

import com.playground.user_manager.user.dataaccess.UserEntity;
import com.playground.user_manager.user.dataaccess.UserRepository;
import com.playground.user_manager.user.messaging.producers.UserMessageProducer;
import com.playground.user_manager.user.service.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMessageProducer userMessageProducer;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testGetUserByAlias() {
        //given
        var alias = "test-user";
        var uuid = UUID.randomUUID();
        when(userRepository.findByAlias(alias)).thenReturn(Optional.of(UserEntity.builder().alias(alias).id(uuid).build()));
        //when
        var user = userService.getUserByAlias(alias);
        //then
        assertAll(
                () -> assertNotNull(user),
                () -> assertEquals(uuid.toString(), user.getId()),
                () -> assertEquals(alias, user.getAlias())
        );
    }

    @Test
    void testGetAllUsers() {
        //given
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        var user1 = UserEntity.builder().id(id1).alias("test-user1").build();
        var user2 = UserEntity.builder().id(id2).alias("test-user2").build();
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
        //when
        var users = userService.getAllUsers();
        //then
        assertAll(
                () -> assertNotNull(users),
                () -> assertEquals(2, users.size())
        );
    }
}