package com.playground.user_manager.auth.service.code_generation.handlers;

import com.playground.user_manager.auth.service.code_generation.CodeGenerationConfig;
import com.playground.user_manager.auth.service.code_generation.CodeGenerationContext;
import com.playground.user_manager.auth.service.code_generation.CodeGenerationHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CodeSaverHandler implements CodeGenerationHandler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CodeGenerationConfig codeGenerationConfig;

    @Override
    public void handle(CodeGenerationContext context) {
        var email = context.getEmail();
        var code = context.getCode();
        var keyPrefix = codeGenerationConfig.getKeyPrefix();
        var CODE_TTL = codeGenerationConfig.getCodeExpirationTime();
        var key = String.format("%s:%s", keyPrefix, email);
        redisTemplate.opsForValue().set(key, code, CODE_TTL);
        log.info("Saved code for {} in Redis with TTL {} minutes", email, CODE_TTL.toMinutes());
    }
}
