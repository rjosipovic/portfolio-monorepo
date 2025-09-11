package com.playground.challenge_manager.errors.exceptions.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {

    // Generic
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G001", "Internal server error"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "G002", "Validation failed"),
    NO_RESOURCE_FOUND(HttpStatus.NOT_FOUND, "G003", "No resource found");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
