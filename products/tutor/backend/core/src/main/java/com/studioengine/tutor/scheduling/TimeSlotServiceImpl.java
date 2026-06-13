package com.studioengine.tutor.scheduling;

import com.studioengine.tutor.dataaccess.enums.TimeSlotState;
import com.studioengine.tutor.dataaccess.repositories.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
public class TimeSlotServiceImpl implements TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;

    @Override
    public List<AvailableSlot> getAvailability(LocalDate from, LocalDate to) {
        return timeSlotRepository.findBySlotDateBetweenAndState(from, to, TimeSlotState.AVAILABLE)
                .stream()
                .map(slot -> AvailableSlot.builder()
                        .id(slot.getId())
                        .date(slot.getSlotDate())
                        .startTime(slot.getStartTime())
                        .endTime(slot.getEndTime())
                        .build())
                .toList();
    }
}

