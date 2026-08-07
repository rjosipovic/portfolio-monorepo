package com.studioengine.tutor.selfservice;

public interface SelfServiceManager {

    AppointmentDetails validateToken(String token);

    AppointmentCancellation confirmCancellation(String token);

    RescheduleInitiation confirmReschedule(String token);
}
