package com.playground.user_manager.user;

import com.playground.user_manager.user.dataaccess.UserEntity;
import com.playground.user_manager.user.dataaccess.UserRepository;
import com.playground.user_manager.user.service.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testGetAllUsers() {
        //given
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        var user1 = UserEntity.create("test-user1", "someemail1@gmail.com", null, null);
        var user2 = UserEntity.create("test-user2", "somemail2@gmail.com", null, null);
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