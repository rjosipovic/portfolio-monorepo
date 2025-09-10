package com.playground.user_manager.errors.custom;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserManagerError {

    private final String message;
    private final String code;
    private final String reason;
}
