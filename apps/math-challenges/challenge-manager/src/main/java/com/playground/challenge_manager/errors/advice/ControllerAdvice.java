package com.playground.challenge_manager.errors.advice;

import com.playground.challenge_manager.errors.custom.ChallengeManagerError;
import com.playground.challenge_manager.errors.exceptions.base.ChallengeManagerException;
import com.playground.challenge_manager.errors.exceptions.enums.ErrorCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;
import java.util.stream.Collectors;

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

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ChallengeManagerError> handleEntityNotFoundException(EntityNotFoundException ex) {
        var errorCode = ErrorCode.NO_RESOURCE_FOUND;
        var details = ex.getMessage();
        var apiError = new ChallengeManagerError(errorCode.getMessage(), errorCode.getCode(), details);
        return new ResponseEntity<>(apiError, errorCode.getHttpStatus());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ChallengeManagerError> handleMissingParameterException(MissingServletRequestParameterException ex) {
        var errorCode = ErrorCode.VALIDATION_FAILED;

        var parameterName = ex.getParameterName();

        var reason = String.format("Required parameter '%s' is missing.", parameterName);

        var apiError = new ChallengeManagerError(errorCode.getMessage(), errorCode.getCode(), reason);
        return new ResponseEntity<>(apiError, errorCode.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ChallengeManagerError> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        var errorCode = ErrorCode.VALIDATION_FAILED;

        String reason;
        // Check if the required type is an enum
        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            // If it is, provide a helpful message with the allowed values.
            var allowedValues = Arrays.stream(ex.getRequiredType().getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            reason = String.format("Invalid value '%s' for parameter '%s'. Allowed values are: [%s].",
                    ex.getValue(), ex.getName(), allowedValues);
        } else {
            // For non-enum types, provide a more generic message.
            reason = String.format("Invalid value '%s' for parameter '%s'. Expected type is '%s'.",
                    ex.getValue(), ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        }
        var apiError = new ChallengeManagerError(errorCode.getMessage(), errorCode.getCode(), reason);
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
    public ResponseEntity<ChallengeManagerError> handleChallengeManagerException(ChallengeManagerException ex) {
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
