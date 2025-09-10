package com.playground.user_manager.auth.service.code_generation;

import lombok.Getter;
import lombok.Setter;

@Getter
public class CodeGenerationContext {

    private String email;
    @Setter
    private String code;

    public CodeGenerationContext(String email) {
        this.email = email;
    }
}
