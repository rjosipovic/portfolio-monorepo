package com.playground.user_manager.auth.service.code_generation;

import com.playground.user_manager.auth.service.code_generation.handlers.CodeGeneratorHandler;
import com.playground.user_manager.auth.service.code_generation.handlers.CodeSaverHandler;
import com.playground.user_manager.auth.service.code_generation.handlers.CodeSenderHandler;
import com.playground.user_manager.auth.service.code_generation.handlers.UserRegisteredHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CodeGenerationChainConfig {

    private final UserRegisteredHandler userRegisteredHandler;
    private final CodeSaverHandler codeSaverHandler;
    private final CodeSenderHandler codeSenderHandler;

    @Bean
    public CodeGenerationChain codeGenerationChain() {
        var chain = new CodeGenerationChain();
        chain.addHandler(userRegisteredHandler);
        chain.addHandler(new CodeGeneratorHandler());
        chain.addHandler(codeSaverHandler);
        chain.addHandler(codeSenderHandler);
        return chain;
    }
}
