package com.playground.user_manager.user;

import com.playground.user_manager.user.dataaccess.UserEntity;
import com.playground.user_manager.user.dataaccess.UserRepository;
import com.playground.user_manager.user.mappers.UserMapper;
import com.playground.user_manager.user.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    private UserServiceImpl userService;

    @BeforeEach
    void init() {
        userService = new UserServiceImpl(userRepository, userMapper);
    }

    @Test
    void testGetAllUsers() {
        //given
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        var user1 = mock(UserEntity.class);
        var user2 = mock(UserEntity.class);
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
        when(user1.getId()).thenReturn(id1);
        when(user2.getId()).thenReturn(id2);
        //when
        var users = userService.getAllUsers();
        //then
        assertAll(
                () -> assertNotNull(users),
                () -> assertEquals(2, users.size())
        );
    }
}