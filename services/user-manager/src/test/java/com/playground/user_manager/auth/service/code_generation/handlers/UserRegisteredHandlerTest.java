package com.playground.user_manager.auth.service.code_generation.handlers;

import com.playground.user_manager.auth.service.RegistrationService;
import com.playground.user_manager.auth.service.code_generation.CodeGenerationContext;
import com.playground.user_manager.errors.exceptions.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRegisteredHandlerTest {

    @Mock
    private RegistrationService registrationService;

    @InjectMocks
    private UserRegisteredHandler handler;

    @Test
    void handle_shouldPass_whenUserIsRegistered() {
        // given
        var email = "test@example.com";
        var ctx = mock(CodeGenerationContext.class);
        when(ctx.getEmail()).thenReturn(email);
        when(registrationService.isRegistered(email)).thenReturn(true);

        //when/then
        handler.handle(ctx); // should not throw
    }

    @Test
    void handle_shouldThrow_whenUserIsNotRegistered() {
        // given
        var email = "notfound@example.com";
        var ctx = mock(CodeGenerationContext.class);
        when(ctx.getEmail()).thenReturn(email);
        when(registrationService.isRegistered(email)).thenReturn(false);

        // when/then
        assertThrows(UserNotFoundException.class, () -> handler.handle(ctx));
    }
}