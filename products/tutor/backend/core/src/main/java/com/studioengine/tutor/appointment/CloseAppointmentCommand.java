package com.studioengine.tutor.appointment;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CloseAppointmentCommand {

    UUID appointmentId;
    CloseOutcome outcome;
    boolean sendFollowup;

    public enum CloseOutcome {
        COMPLETED,
        NO_SHOW;
    }
}
