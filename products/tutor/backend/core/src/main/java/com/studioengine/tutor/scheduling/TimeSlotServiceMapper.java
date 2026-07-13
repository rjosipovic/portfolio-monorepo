package com.studioengine.tutor.scheduling;

import com.studioengine.tutor.dataaccess.entities.TimeSlot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TimeSlotServiceMapper {

    @Mapping(source = "slotDate", target = "date")
    AvailableSlot toAvailableSlot(TimeSlot slot);

    @Mapping(source = "slotDate", target = "date")
    CreatedSlot toCreatedSlot(TimeSlot slot);
}
