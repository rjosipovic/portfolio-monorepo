package com.studioengine.tutor.selfservice;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AppointmentCancellation {

    UUID appointmentId;
    String message;
}
