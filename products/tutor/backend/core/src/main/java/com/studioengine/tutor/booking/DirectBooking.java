package com.studioengine.tutor.booking;

import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DirectBooking {

    UUID appointmentId;
    AppointmentState state;
    UUID timeSlotId;
    LocalDate slotDate;
    LocalTime startTime;
    UUID studentId;
    String studentName;
    UUID serviceCategoryId;
    String serviceCategoryName;
}
