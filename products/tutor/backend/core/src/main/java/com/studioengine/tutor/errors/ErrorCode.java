package com.studioengine.tutor.errors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    SLOT_CONFLICT("T001", "Slot already reserved"),
    INVALID_RESERVATION("T002", "Reservation expired or invalid"),
    INVALID_STATE_TRANSITION("T003", "Invalid state transition"),
    WEBHOOK_VERIFICATION_FAILED("T004", "Webhook signature verification failed"),
    DEADLINE_PASSED("T005", "Cancellation/reschedule deadline passed"),
    TOKEN_EXPIRED("T006", "Token expired or already used"),
    SLOT_WITHDRAWAL_BLOCKED("T007", "Cannot withdraw slot with active appointment"),
    PREMATURE_CLOSURE("T008", "Cannot close appointment before end time"),
    MISSING_CANCELLATION_REASON("T009", "Cancellation reason required"),
    AUTHENTICATION_FAILED("T010", "Missing or invalid JWT"),
    OTP_VERIFICATION_FAILED("T011", "OTP expired or invalid"),
    RESOURCE_NOT_FOUND("T012", "Resource not found"),
    PRE_BOOKED_SELF_SERVICE("T013", "Self-service not available for direct bookings"),
    CHECKOUT_VALIDATION_FAILED("T014", "Checkout validation failed"),
    EMAIL_ALREADY_IN_USE("T15", "Email already in use");

    private final String code;
    private final String message;
}
