package com.studioengine.tutor.appointment;

import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CanceledAppointment {

    UUID appointmentId;
    AppointmentState state;
}
