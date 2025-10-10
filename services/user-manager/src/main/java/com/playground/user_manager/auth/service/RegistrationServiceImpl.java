package com.playground.user_manager.auth.service;

import com.playground.user_manager.auth.api.dto.RegisterUserRequest;
import com.playground.user_manager.auth.api.dto.RegisteredUser;
import com.playground.user_manager.errors.exceptions.UserAlreadyExistsException;
import com.playground.user_manager.user.dataaccess.UserRepository;
import com.playground.user_manager.user.mappers.UserMapper;
import com.playground.user_manager.user.messaging.producers.UserMessageProducer;
import com.playground.user_manager.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final UserRepository userRepository;
    private final UserMessageProducer userMessageProducer;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void register(RegisterUserRequest registerUserRequest) {
        verifyUniqueness(registerUserRequest.getAlias(), registerUserRequest.getEmail());

        var userEntity = userMapper.toEntity(registerUserRequest);
        var savedUserEntity = userRepository.save(userEntity);
        var user = userMapper.toModel(savedUserEntity);

        publishUserCreatedMessage(user);
    }

    private void verifyUniqueness(String alias, String email) {
        if (userRepository.findByAlias(alias).isPresent()) {
            throw new UserAlreadyExistsException(String.format("User with alias: %s already exists", alias));
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException(String.format("User with email: %s already exists", email));
        }
    }

    @Override
    public boolean isRegistered(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public Optional<RegisteredUser> getRegisteredUser(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toRegisteredUserDto);
    }

    private void publishUserCreatedMessage(User user) {
        userMessageProducer.sendUserCreatedMessage(user);
    }
}
