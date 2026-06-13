package com.studioengine.tutor.scheduling;

public interface ReservationService {

    Reservation reserve(ReserveSlotCommand command);
}
