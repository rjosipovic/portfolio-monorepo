package com.playground.user_manager.auth.service.code_generation.handlers;

import com.playground.user_manager.auth.service.code_generation.CodeGenerationContext;
import com.playground.user_manager.auth.service.code_generation.CodeGenerationHandler;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;

@Slf4j
public class CodeGeneratorHandler implements CodeGenerationHandler {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public void handle(CodeGenerationContext context) {
        log.info("Generating code");
        // Generate a random 6-digit code (zero-padded)
        var code = 100_000 + RANDOM.nextInt(900_000);
        var codeStr = String.valueOf(code);
        context.setCode(codeStr);
    }
}
