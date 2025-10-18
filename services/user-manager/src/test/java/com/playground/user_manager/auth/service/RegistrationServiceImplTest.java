package com.playground.user_manager.auth.service;

import com.playground.user_manager.auth.api.dto.RegisterUserRequest;
import com.playground.user_manager.user.dataaccess.UserEntity;
import com.playground.user_manager.user.dataaccess.UserRepository;
import com.playground.user_manager.user.mappers.UserMapper;
import com.playground.user_manager.user.messaging.producers.UserMessageProducer;
import com.playground.user_manager.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMessageProducer userMessageProducer;

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    private RegistrationServiceImpl registrationService;

    @BeforeEach
    void init() {
        registrationService = new RegistrationServiceImpl(userRepository, userMessageProducer, userMapper);
    }

    @Test
    void testRegisterUser() {
        //given
        var alias = "test-user";
        var email = "someemail@gmail.com";
        var uuid = UUID.randomUUID();
        var registerUserRequest = RegisterUserRequest.builder().alias(alias).email(email).build();
        var userEntity = mock(UserEntity.class);
        when(userRepository.findByAlias(alias)).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(userEntity.getId()).thenReturn(uuid);
        when(userEntity.getAlias()).thenReturn(alias);
        //when
        registrationService.register(registerUserRequest);
        //then
        var userEntityCapture = ArgumentCaptor.forClass(UserEntity.class);
        var userCapture = ArgumentCaptor.forClass(User.class);

        verify(userRepository, times(1)).findByAlias(alias);
        verify(userRepository, times(1)).save(userEntityCapture.capture());
        verify(userMessageProducer, times(1)).sendUserCreatedMessage(userCapture.capture());

        assertAll(
                () -> assertEquals(alias, userEntityCapture.getValue().getAlias()),
                () -> assertEquals(email, userEntityCapture.getValue().getEmail()),
                () -> assertEquals(alias, userCapture.getValue().getAlias()),
                () -> assertEquals(uuid.toString(), userCapture.getValue().getId())
        );
    }
}