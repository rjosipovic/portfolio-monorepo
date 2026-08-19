package com.studioengine.tutor.scheduling;

import java.time.LocalDate;
import java.util.List;

public interface TimeSlotService {

    List<CreatedSlot> getSlotsByDateRange(LocalDate from, LocalDate to);

    List<AvailableSlot> getAvailability(LocalDate from, LocalDate to);

    List<CreatedSlot> createSlots(CreateSlotsCommand command);

    List<CreatedSlot> publishSlots(PublishSlotsCommand command);

    void deleteSlots(DeleteSlotsCommand command);

    void withdrawSlots(WithdrawSlotsCommand command);
}

