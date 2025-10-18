package com.playground.analytics_manager.inbound.user.lifecycle;

import com.playground.analytics_manager.dataaccess.repository.UserRepository;
import com.playground.analytics_manager.inbound.user.model.User;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class UserLifecycleCreateHandlerTest {

    @Mock
    private UserRepository userRepository;

    private Validator validator;

    private UserLifecycleCreateHandler userLifecycleCreateHandler;

    @BeforeEach
    void setUp() {
        var factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        userLifecycleCreateHandler = new UserLifecycleCreateHandler(userRepository, validator);
    }

    @Test
    void whenHandleUserWithInvalidData_thenThrowException() {
        // given
        // This User model is invalid because its alias is blank
        var user = User.builder()
                .id(UUID.randomUUID().toString())
                .alias("")
                .build();

        // when & then
        // Assert that processing this event throws a ConstraintViolationException
        // because the resulting UserEntity will fail bean validation.
        assertThrows(ConstraintViolationException.class, () -> userLifecycleCreateHandler.handle(user));
    }
}
