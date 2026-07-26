package com.studioengine.tutor.errors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(TutorEngineException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(TutorEngineException ex) {
        var errorCode = ex.getErrorCode();
        var status = ErrorHttpStatusMapping.resolve(errorCode);

        if (status.is5xxServerError()) {
            log.error("Business exception {} - {}", errorCode.getCode(), ex.getDetail());
        } else {
            log.warn("Business exception {} - {}", errorCode.getCode(), ex.getDetail());
        }

        var response = ErrorResponse.builder()
                .message(errorCode.getMessage())
                .code(errorCode.getCode())
                .reason(ex.getDetail())
                .build();

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());

        var details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("%s %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining(", "));

        var response = ErrorResponse.builder()
                .message("Validation failed")
                .code("VALIDATION")
                .reason(details)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        log.warn("Missing request parameter: {}", ex.getParameterName());

        var response = ErrorResponse.builder()
                .message("Missing required parameter")
                .code("VALIDATION")
                .reason("Parameter '%s' is required".formatted(ex.getParameterName()))
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Type mismatch: {}", ex.getMessage());

        var response = ErrorResponse.builder()
                .message("Invalid parameter type")
                .code("VALIDATION")
                .reason("Parameter '%s' must be of type %s".formatted(ex.getName(), ex.getRequiredType().getSimpleName()))
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
        log.error("Unexpected exception: {}", ex.getMessage(), ex);

        var response = ErrorResponse.builder()
                .message("Internal server error")
                .code("INTERNAL")
                .reason(null)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
