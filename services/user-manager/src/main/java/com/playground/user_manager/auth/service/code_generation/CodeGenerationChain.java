package com.playground.user_manager.auth.service.code_generation;

import java.util.LinkedList;
import java.util.List;

public class CodeGenerationChain {

    private final List<CodeGenerationHandler> handlers = new LinkedList<>();

    public void addHandler(CodeGenerationHandler handler) {
        handlers.add(handler);
    }

    public void handle(CodeGenerationContext context) {
        handlers.forEach(handler -> handler.handle(context));
    }
}
