package com.playground.user_manager.user.service;

import com.playground.user_manager.errors.exceptions.UserNotFoundException;
import com.playground.user_manager.user.dataaccess.UserRepository;
import com.playground.user_manager.user.dataaccess.UserEntity;
import com.playground.user_manager.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        return StreamSupport.stream(userRepository.findAll().spliterator(), false)
                .map(toUserModel)
                .toList();
    };

    public List<User> getUsersByIds(List<String> ids) {
        var uuids = ids.stream().map(UUID::fromString).toList();
        return userRepository.findByIdIn(uuids)
                .stream()
                .map(toUserModel)
                .toList();
    };

    @Override
    public User getUserByAlias(String alias) {
        return userRepository
                .findByAlias(alias)
                .map(toUserModel)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with alias %s not found", alias)));
    }

    private static final Function<UserEntity, User> toUserModel = userEntity ->
            User.builder()
                    .id(userEntity.getId().toString())
                    .alias(userEntity.getAlias())
                    .build();
}
