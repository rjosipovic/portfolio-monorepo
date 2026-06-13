package com.studioengine.tutor.errors;

import lombok.Getter;

@Getter
public abstract class TutorEngineException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String detail;

    protected TutorEngineException(ErrorCode errorCode, String detail) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = detail;
    }
}
