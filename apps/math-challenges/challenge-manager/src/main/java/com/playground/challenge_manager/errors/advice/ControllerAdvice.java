package com.playground.challenge_manager.errors.advice;

import com.playground.challenge_manager.errors.custom.ChallengeManagerError;
import com.playground.challenge_manager.errors.exceptions.base.ChallengeManagerException;
import com.playground.challenge_manager.errors.exceptions.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Slf4j
public class ControllerAdvice {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ChallengeManagerError> handleGenericException(Exception ex) {
        log.error(ex.getMessage(), ex);

        var errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        var apiError = new ChallengeManagerError(errorCode.getMessage(), errorCode.getCode(), "An error occurred while processing request");
        return new ResponseEntity<>(apiError, errorCode.getHttpStatus());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ChallengeManagerError> handleNoResourceFoundException(NoResourceFoundException ex) {
        var errorCode = ErrorCode.NO_RESOURCE_FOUND;
        var details = ex.getMessage();
        var apiError = new ChallengeManagerError(errorCode.getMessage(), errorCode.getCode(), details);
        return new ResponseEntity<>(apiError, errorCode.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ChallengeManagerError> handleValidationException(MethodArgumentNotValidException ex) {
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

        var apiError = new ChallengeManagerError(errorCode.getMessage(), errorCode.getCode(), reason);
        return new ResponseEntity<>(apiError, errorCode.getHttpStatus());
    }

    @ExceptionHandler(ChallengeManagerException.class)
    public ResponseEntity<ChallengeManagerError> handleUserManagerException(ChallengeManagerException ex) {
        var errorCode = ex.getErrorCode();

        if (errorCode.getHttpStatus().is5xxServerError()) {
            log.error("ChallengeManagerError occurred: {}", ex.getDetail(), ex);
        } else {
            log.warn("ChallengeManagerError occurred: {}", ex.getDetail(), ex);
        }

        var apiError = new ChallengeManagerError(errorCode.getMessage(), errorCode.getCode(), ex.getDetail());
        return new ResponseEntity<>(apiError, errorCode.getHttpStatus());
    }
}
