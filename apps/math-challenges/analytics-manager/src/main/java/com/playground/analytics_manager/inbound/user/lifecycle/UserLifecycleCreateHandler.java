package com.playground.analytics_manager.inbound.user.lifecycle;

import com.playground.analytics_manager.dataaccess.entity.UserEntity;
import com.playground.analytics_manager.dataaccess.repository.UserRepository;
import com.playground.analytics_manager.inbound.user.model.User;
import com.playground.analytics_manager.inbound.user.model.UserLifecycleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserLifecycleCreateHandler implements UserLifecycleHandler {

    private final UserRepository userRepository;

    @Override
    public void handle(User user) {
        log.info("User created: {}", user);
        var userEntity = toUserEntity(user);
        userRepository.save(userEntity);
    }

    @Override
    public UserLifecycleType supports() {
        return UserLifecycleType.CREATED;
    }

    private UserEntity toUserEntity(User user) {
        return UserEntity.builder()
                .id(UUID.fromString(user.getId()))
                .alias(user.getAlias())
                .build();
    }
}
