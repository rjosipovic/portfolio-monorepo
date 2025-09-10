package com.playground.user_manager.auth.service.code_generation.handlers;

import com.playground.user_manager.auth.messaging.AuthNotification;
import com.playground.user_manager.auth.messaging.producers.AuthMessageProducer;
import com.playground.user_manager.auth.service.code_generation.CodeGenerationConfig;
import com.playground.user_manager.auth.service.code_generation.CodeGenerationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeSenderHandlerTest {

    @Mock
    private AuthMessageProducer authMessageProducer;
    @Mock
    private CodeGenerationConfig codeGenerationConfig;
    @InjectMocks
    private CodeSenderHandler handler;


    @Test
    void handle_sendsCorrectNotification() {
        // given
        var email = "user@example.com";
        var code = "123456";
        var context = new CodeGenerationContext(email);
        context.setCode(code);
        var subject = "Challenges Access Code";
        var messageTemplate = "Your code is: %s";
        when(codeGenerationConfig.getSubject()).thenReturn(subject);
        when(codeGenerationConfig.getNotificationMessageTemplate()).thenReturn(messageTemplate);

        // when
        handler.handle(context);

        // then
        var captor = ArgumentCaptor.forClass(AuthNotification.class);
        verify(authMessageProducer, times(1)).sendAuthCode(captor.capture());

        var notification = captor.getValue();

        assertAll(
                () -> assertThat(notification.getTo()).isEqualTo(email),
                () -> assertThat(notification.getSubject()).isEqualTo(subject),
                () -> assertThat(notification.getBody()).isEqualTo(String.format(messageTemplate, code))
        );
    }
}