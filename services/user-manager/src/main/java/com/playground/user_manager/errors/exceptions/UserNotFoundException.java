package com.playground.user_manager.errors.exceptions;

import com.playground.user_manager.errors.exceptions.base.UserManagerException;
import com.playground.user_manager.errors.exceptions.enums.ErrorCode;

/**
 * Exception thrown when a user is not found.
 */
public class UserNotFoundException extends UserManagerException {

    public UserNotFoundException(String detail) {
        super(ErrorCode.USER_NOT_FOUND, detail);
    }
}
