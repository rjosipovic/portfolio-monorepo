package com.playground.user_manager.errors.exceptions.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {

    // User errors
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "User not found"),
    USER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "U002", "User already exists"),

    // Auth errors
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "A001", "Invalid verification code"),
    TOKEN_GENERATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "A002", "Failed to generate token"),

    // Generic
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G001", "Internal server error"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "G002", "Validation failed");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
