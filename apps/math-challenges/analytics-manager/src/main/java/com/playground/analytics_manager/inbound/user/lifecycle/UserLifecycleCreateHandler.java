package com.playground.analytics_manager.inbound.user.lifecycle;

import com.playground.analytics_manager.dataaccess.entity.UserEntity;
import com.playground.analytics_manager.dataaccess.repository.UserRepository;
import com.playground.analytics_manager.inbound.user.model.User;
import com.playground.analytics_manager.inbound.user.model.UserLifecycleType;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserLifecycleCreateHandler implements UserLifecycleHandler {

    private final UserRepository userRepository;
    private final Validator validator;

    @Override
    public void handle(User user) {
        log.info("User created: {}", user);
        var userEntity = toUserEntity(user);

        // Manually trigger bean validation before saving
        var violations = validator.validate(userEntity);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        userRepository.save(userEntity);
    }

    @Override
    public UserLifecycleType supports() {
        return UserLifecycleType.CREATED;
    }

    private UserEntity toUserEntity(User user) {
        return UserEntity.create(UUID.fromString(user.getId()), user.getAlias());
    }
}
