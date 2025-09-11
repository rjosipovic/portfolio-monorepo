package com.playground.user_manager.auth.service.code_generation.handlers;

import com.playground.user_manager.auth.messaging.AuthNotification;
import com.playground.user_manager.auth.messaging.producers.AuthMessageProducer;
import com.playground.user_manager.auth.service.code_generation.CodeGenerationConfig;
import com.playground.user_manager.auth.service.code_generation.CodeGenerationContext;
import com.playground.user_manager.auth.service.code_generation.CodeGenerationHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CodeSenderHandler implements CodeGenerationHandler {

    private final AuthMessageProducer authMessageProducer;
    private final CodeGenerationConfig codeGenerationConfig;

    @Override
    public void handle(CodeGenerationContext context) {
        log.info("Sending code to {}", context.getEmail());
        var email = context.getEmail();
        var code = context.getCode();
        var subject = codeGenerationConfig.getSubject();
        var messageTemplate = codeGenerationConfig.getNotificationMessageTemplate();
        var message = String.format(messageTemplate, code);
        var authNotification = AuthNotification.builder()
                .to(email)
                .subject(subject)
                .body(message)
                .build();
        authMessageProducer.sendAuthCode(authNotification);
    }
}
