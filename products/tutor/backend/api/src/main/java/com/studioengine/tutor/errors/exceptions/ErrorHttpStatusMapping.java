package com.studioengine.tutor.errors.exceptions;

import com.studioengine.tutor.errors.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static com.studioengine.tutor.errors.ErrorCode.AUTHENTICATION_FAILED;
import static com.studioengine.tutor.errors.ErrorCode.CHECKOUT_VALIDATION_FAILED;
import static com.studioengine.tutor.errors.ErrorCode.DEADLINE_PASSED;
import static com.studioengine.tutor.errors.ErrorCode.INVALID_RESERVATION;
import static com.studioengine.tutor.errors.ErrorCode.INVALID_STATE_TRANSITION;
import static com.studioengine.tutor.errors.ErrorCode.MISSING_CANCELLATION_REASON;
import static com.studioengine.tutor.errors.ErrorCode.OTP_VERIFICATION_FAILED;
import static com.studioengine.tutor.errors.ErrorCode.PREMATURE_CLOSURE;
import static com.studioengine.tutor.errors.ErrorCode.PRE_BOOKED_SELF_SERVICE;
import static com.studioengine.tutor.errors.ErrorCode.RESOURCE_NOT_FOUND;
import static com.studioengine.tutor.errors.ErrorCode.SLOT_CONFLICT;
import static com.studioengine.tutor.errors.ErrorCode.SLOT_WITHDRAWAL_BLOCKED;
import static com.studioengine.tutor.errors.ErrorCode.TOKEN_EXPIRED;
import static com.studioengine.tutor.errors.ErrorCode.WEBHOOK_VERIFICATION_FAILED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.GONE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public final class ErrorHttpStatusMapping {

    private static final Map<ErrorCode, HttpStatus> MAPPING = Map.ofEntries(
            Map.entry(SLOT_CONFLICT, CONFLICT),
            Map.entry(INVALID_RESERVATION, BAD_REQUEST),
            Map.entry(INVALID_STATE_TRANSITION, BAD_REQUEST),
            Map.entry(WEBHOOK_VERIFICATION_FAILED, BAD_REQUEST),
            Map.entry(DEADLINE_PASSED, FORBIDDEN),
            Map.entry(TOKEN_EXPIRED, GONE),
            Map.entry(SLOT_WITHDRAWAL_BLOCKED, BAD_REQUEST),
            Map.entry(PREMATURE_CLOSURE, BAD_REQUEST),
            Map.entry(MISSING_CANCELLATION_REASON, BAD_REQUEST),
            Map.entry(AUTHENTICATION_FAILED, UNAUTHORIZED),
            Map.entry(OTP_VERIFICATION_FAILED, UNAUTHORIZED),
            Map.entry(RESOURCE_NOT_FOUND, NOT_FOUND),
            Map.entry(PRE_BOOKED_SELF_SERVICE, BAD_REQUEST),
            Map.entry(CHECKOUT_VALIDATION_FAILED, BAD_REQUEST)
    );

    public static HttpStatus resolve(ErrorCode errorCode) {
        return MAPPING.getOrDefault(errorCode, INTERNAL_SERVER_ERROR);
    }
}
