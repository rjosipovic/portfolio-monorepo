package com.studioengine.tutor.scheduling;

import com.studioengine.tutor.dataaccess.entities.TimeSlot;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.dataaccess.enums.TimeSlotState;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import com.studioengine.tutor.dataaccess.repositories.TimeSlotRepository;
import com.studioengine.tutor.dataaccess.repositories.TimeSlotStateLogRepository;
import com.studioengine.tutor.errors.exceptions.ResourceNotFoundException;
import com.studioengine.tutor.errors.exceptions.SlotConflictException;
import com.studioengine.tutor.errors.exceptions.SlotWithdrawalBlockedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class TimeSlotServiceImpl implements TimeSlotService {

    private static final Set<AppointmentState> ACTIVE_APPOINTMENT_STATES = Set.of(
            AppointmentState.RESERVED, AppointmentState.PAID, AppointmentState.CONFIRMED,
            AppointmentState.PRE_BOOKED, AppointmentState.PENDING_PAYMENT
    );

    private final TimeSlotRepository timeSlotRepository;
    private final TimeSlotStateMachine stateMachine;
    private final AppointmentRepository appointmentRepository;
    private final TimeSlotServiceMapper timeSlotServiceMapper;
    private final TimeSlotStateLogRepository timeSlotStateLogRepository;

    @Override
    public List<CreatedSlot> getSlotsByDateRange(LocalDate from, LocalDate to) {
        return timeSlotRepository.findBySlotDateBetween(from, to).stream()
                .map(timeSlotServiceMapper::toCreatedSlot)
                .toList();
    }

    @Override
    public List<AvailableSlot> getAvailability(LocalDate from, LocalDate to) {
        return timeSlotRepository.findBySlotDateBetweenAndState(from, to, TimeSlotState.AVAILABLE)
                .stream()
                .map(timeSlotServiceMapper::toAvailableSlot)
                .toList();
    }

    @Override
    @Transactional
    public List<CreatedSlot> createSlots(CreateSlotsCommand command) {
        var slotDefinitions = command.getSlots();

        slotDefinitions.forEach(this::verifySlotNotExists);

        var slots = slotDefinitions.stream()
                .map(def -> TimeSlot.create(def.getDate(), def.getStartTime()))
                .toList();

        return timeSlotRepository.saveAll(slots).stream()
                .map(timeSlotServiceMapper::toCreatedSlot)
                .toList();
    }

    @Override
    @Transactional
    public List<CreatedSlot> publishSlots(PublishSlotsCommand command) {
        var slots = findAllByIds(command.getSlotIds());

        slots.forEach(slot -> stateMachine.transition(slot, TimeSlotState.AVAILABLE, "TUTOR"));
        timeSlotRepository.saveAll(slots);

        return slots.stream()
                .map(timeSlotServiceMapper::toCreatedSlot)
                .toList();
    }

    @Override
    @Transactional
    public void withdrawSlots(WithdrawSlotsCommand command) {
        var slots = findAllByIds(command.getSlotIds());
        verifyNoActiveAppointments(slots);
        slots.forEach(slot -> stateMachine.transition(slot, TimeSlotState.DRAFT, "TUTOR"));
        timeSlotRepository.saveAll(slots);
    }

    @Override
    @Transactional
    public void deleteSlots(DeleteSlotsCommand command) {
        var slots = findAllByIds(command.getSlotIds());
        verifyNoActiveAppointments(slots);
        var slotIds = slots.stream().map(TimeSlot::getId).toList();
        timeSlotStateLogRepository.deleteAllByTimeSlotIdIn(slotIds);
        timeSlotRepository.deleteAll(slots);
    }

    // --- Private helpers ---
    private List<TimeSlot> findAllByIds(List<UUID> ids) {
        var slots = timeSlotRepository.findAllById(ids);
        if (slots.size() != ids.size()) {
            throw new ResourceNotFoundException("One or more TimeSlots not found");
        }
        return slots;
    }

    private void verifyNoActiveAppointments(List<TimeSlot> slots) {
        var slotIds = slots.stream().map(TimeSlot::getId).toList();
        var blockers = appointmentRepository.findByTimeSlotIdInAndStateIn(slotIds, ACTIVE_APPOINTMENT_STATES);
        if (!blockers.isEmpty()) {
            var blockedSlotId = blockers.getFirst().getTimeSlot().getId();
            throw new SlotWithdrawalBlockedException(
                    "Slot %s has an active appointment".formatted(blockedSlotId)
            );
        }
    }

    private void verifySlotNotExists(CreateSlotsCommand.SlotDefinition def) {
        var date = def.getDate();
        var startTime = def.getStartTime();
        if (timeSlotRepository.existsBySlotDateAndStartTime(date, startTime)) {
            throw new SlotConflictException("Slot already exists for %s at %s".formatted(date, startTime));
        }
    }
}

