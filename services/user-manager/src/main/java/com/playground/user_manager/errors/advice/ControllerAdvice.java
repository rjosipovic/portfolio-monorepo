package com.playground.user_manager.errors.advice;

import com.playground.user_manager.errors.custom.UserManagerError;
import com.playground.user_manager.errors.exceptions.base.UserManagerException;
import com.playground.user_manager.errors.exceptions.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ControllerAdvice {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<UserManagerError> handleGenericException(Exception ex) {
        log.error(ex.getMessage(), ex);

        var errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        var apiError = new UserManagerError(errorCode.getMessage(), errorCode.getCode(), "An error occurred while processing request");
        return new ResponseEntity<>(apiError, errorCode.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<UserManagerError> handleValidationException(MethodArgumentNotValidException ex) {
        var errorCode = ErrorCode.VALIDATION_FAILED;
        var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .toList();
        var globalErrors = ex.getBindingResult().getGlobalErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        var allErrors = new java.util.ArrayList<>(fieldErrors);
        allErrors.addAll(globalErrors);
        var reason = String.join("; ", allErrors);
        var apiError = new UserManagerError(errorCode.getMessage(), errorCode.getCode(), reason);
        return new ResponseEntity<>(apiError, errorCode.getHttpStatus());
    }

    @ExceptionHandler(UserManagerException.class)
    public ResponseEntity<UserManagerError> handleUserManagerException(UserManagerException ex) {
        var errorCode = ex.getErrorCode();

        if (errorCode.getHttpStatus().is5xxServerError()) {
            log.error("UserManagerError occurred: {}", ex.getDetail(), ex);
        } else {
            log.warn("UserManagerError occurred: {}", ex.getDetail());
        }

        var apiError = new UserManagerError(errorCode.getMessage(), errorCode.getCode(), ex.getDetail());
        return new ResponseEntity<>(apiError, errorCode.getHttpStatus());
    }
}
