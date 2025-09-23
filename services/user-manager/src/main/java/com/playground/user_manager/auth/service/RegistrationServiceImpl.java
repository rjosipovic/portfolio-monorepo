package com.playground.user_manager.auth.service;

import com.playground.user_manager.auth.api.dto.RegisterUserRequest;
import com.playground.user_manager.auth.api.dto.RegisteredUser;
import com.playground.user_manager.errors.exceptions.UserAlreadyExistsException;
import com.playground.user_manager.user.dataaccess.UserEntity;
import com.playground.user_manager.user.dataaccess.UserRepository;
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

    @Override
    @Transactional
    public void register(RegisterUserRequest registerUserRequest) {
        var alias = registerUserRequest.getAlias();
        var email = registerUserRequest.getEmail();
        verifyUniqueness(alias, email);
        var birthdate = registerUserRequest.getBirthdate();
        var gender = registerUserRequest.getGender();
        var userEntity = UserEntity.create(alias, email, birthdate, gender);
        var savedUserEntity = userRepository.save(userEntity);
        var user = User.builder().id(savedUserEntity.getId().toString()).alias(savedUserEntity.getAlias()).build();
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
                .map(userEntity -> RegisteredUser.builder()
                        .userId(userEntity.getId().toString())
                        .email(userEntity.getEmail())
                        .alias(userEntity.getAlias())
                        .build()
                );
    }

    private void publishUserCreatedMessage(User user) {
        userMessageProducer.sendUserCreatedMessage(user);
    }
}
