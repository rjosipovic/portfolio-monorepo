package com.playground.user_manager.auth.service.code_generation.handlers;

import com.playground.user_manager.auth.service.code_generation.CodeGenerationContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class CodeGeneratorHandlerTest {

    @Test
    void handle_generatesSixDigitCodeAndSetsItOnContext() {
        // given
        var email = "user@example.com";
        var context = new CodeGenerationContext(email);
        var handler = new CodeGeneratorHandler();

        // when
        handler.handle(context);

        // then
        var code = context.getCode();
        assertAll(
                () -> assertThat(code).isNotNull(),
                () -> assertThat(code).hasSize(6),
                () -> assertThat(code).matches("\\d{6}")
        );
    }
}