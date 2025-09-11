package com.playground.user_manager.errors.exceptions;

import com.playground.user_manager.errors.exceptions.base.UserManagerException;
import com.playground.user_manager.errors.exceptions.enums.ErrorCode;

/**
 * Thrown when user with given alias already exists in the system.
 */
public class UserAlreadyExistsException extends UserManagerException {

    public UserAlreadyExistsException(String details) {
        super(ErrorCode.USER_ALREADY_EXISTS, details);
    }
}
