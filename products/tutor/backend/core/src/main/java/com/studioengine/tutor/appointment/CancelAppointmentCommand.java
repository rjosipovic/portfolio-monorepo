package com.studioengine.tutor.appointment;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CancelAppointmentCommand {

    UUID appointmentId;
    String reason;
}
