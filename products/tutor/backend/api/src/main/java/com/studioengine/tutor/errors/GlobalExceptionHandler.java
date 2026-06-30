package com.studioengine.tutor.errors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
