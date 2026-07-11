package com.studioengine.tutor.student;

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
public class AppointmentHistory {

    UUID appointmentId;
    LocalDate date;
    LocalTime startTime;
    String serviceCategoryName;
    AppointmentState state;
}
