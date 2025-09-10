package com.playground.user_manager.errors.exceptions;

import com.playground.user_manager.errors.exceptions.base.UserManagerException;
import com.playground.user_manager.errors.exceptions.enums.ErrorCode;
import lombok.Getter;

/**
 * Exception thrown when the verification code entered is invalid.
 */
@Getter
public class InvalidVerificationCodeException extends UserManagerException {

    public InvalidVerificationCodeException(String details) {
        super(ErrorCode.INVALID_VERIFICATION_CODE, details);
    }
}
