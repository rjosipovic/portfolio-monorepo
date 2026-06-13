package com.studioengine.tutor.scheduling;

import java.time.LocalDate;
import java.util.List;

public interface TimeSlotService {

    List<AvailableSlot> getAvailability(LocalDate from, LocalDate to);
}

