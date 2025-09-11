package com.playground.user_manager.auth.service;

import com.playground.user_manager.auth.service.code_generation.CodeGenerationChain;
import com.playground.user_manager.auth.service.code_generation.CodeGenerationConfig;
import com.playground.user_manager.auth.service.code_generation.CodeGenerationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final CodeGenerationChain codeGenerationChain;
    private final CodeGenerationConfig codeGenerationConfig;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void generateAuthCode(String email) {
        var context = new CodeGenerationContext(email);
        codeGenerationChain.handle(context);
    }

    @Override
    public boolean verifyCode(String email, String code) {
        var keyPrefix = codeGenerationConfig.getKeyPrefix();
        var key = String.format("%s:%s", keyPrefix, email);
        var storedCode = (String) redisTemplate.opsForValue().get(key);
        var isValid = Objects.equals(storedCode, code);
        if (isValid) {
            redisTemplate.delete(key);
        }
        return isValid;
    }
}
