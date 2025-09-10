package com.playground.user_manager.errors.exceptions.base;


import com.playground.user_manager.errors.exceptions.enums.ErrorCode;
import lombok.Getter;

import java.io.Serial;

/**
 * Base class for all custom exceptions.
 *
 */
@Getter
public abstract class UserManagerException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 202508041318L;

    private final ErrorCode errorCode;
    private final String detail;

    protected UserManagerException(final ErrorCode errorCode, final String detail) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = detail;
    }

    protected UserManagerException(final ErrorCode errorCode, final String detail, final Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.detail = detail;
    }
}
