package com.playground.user_manager.errors.exceptions;

import com.playground.user_manager.errors.exceptions.base.UserManagerException;
import com.playground.user_manager.errors.exceptions.enums.ErrorCode;

/**
 * Exception thrown when there is an error generating a JSON Web Token.
 */
public class TokenGenerationException extends UserManagerException {

    public TokenGenerationException(String details, Throwable cause) {
        super(ErrorCode.TOKEN_GENERATION_ERROR, details, cause);
    }
}
