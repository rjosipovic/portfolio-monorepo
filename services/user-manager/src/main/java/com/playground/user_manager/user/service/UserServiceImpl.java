package com.playground.user_manager.user.service;

import com.playground.user_manager.user.dataaccess.UserRepository;
import com.playground.user_manager.user.mappers.UserMapper;
import com.playground.user_manager.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<User> getAllUsers() {
        return StreamSupport.stream(userRepository.findAll().spliterator(), false)
                .map(userMapper::toModel)
                .toList();
    }

    public List<User> getUsersByIds(List<String> ids) {
        var uuids = ids.stream().map(UUID::fromString).toList();
        return userRepository.findByIdIn(uuids)
                .stream()
                .map(userMapper::toModel)
                .toList();
    }
}
