package com.studioengine.tutor.selfservice;

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
public class AppointmentDetails {

    UUID appointmentId;
    String studentName;
    String serviceCategoryName;
    LocalDate date;
    LocalTime startTime;
    boolean deadlineMissed;
}
