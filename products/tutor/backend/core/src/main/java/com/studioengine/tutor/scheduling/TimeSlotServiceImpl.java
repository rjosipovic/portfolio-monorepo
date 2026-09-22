package com.studioengine.tutor.scheduling;

import com.studioengine.tutor.config.BrandProperties;
import com.studioengine.tutor.dataaccess.entities.TimeSlot;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.dataaccess.enums.TimeSlotState;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import com.studioengine.tutor.dataaccess.repositories.TimeSlotRepository;
import com.studioengine.tutor.dataaccess.repositories.TimeSlotStateLogRepository;
import com.studioengine.tutor.errors.exceptions.InvalidSlotTimeException;
import com.studioengine.tutor.errors.exceptions.PastSlotException;
import com.studioengine.tutor.errors.exceptions.ResourceNotFoundException;
import com.studioengine.tutor.errors.exceptions.SlotConflictException;
import com.studioengine.tutor.errors.exceptions.SlotWithdrawalBlockedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;


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
    private final BrandProperties brandProperties;
    private final Clock clock;

    @Override
    public List<CalendarSlot> getSlotsByDateRange(LocalDate from, LocalDate to) {
        var timeSlots = timeSlotRepository.findBySlotDateBetweenOrderBySlotDateAscStartTimeAsc(from, to);
        var timeSlotIds = timeSlots.stream().map(TimeSlot::getId).toList();

        var appointmentBySlotId = appointmentRepository.findByTimeSlotIdInAndStateIn(timeSlotIds, ACTIVE_APPOINTMENT_STATES).stream()
                .collect(Collectors.toMap(a -> a.getTimeSlot().getId(), Function.identity()));

        return timeSlots.stream()
                .map(slot -> CalendarSlot.builder()
                        .slot(timeSlotServiceMapper.toCreatedSlot(slot))
                        .appointment(Optional.ofNullable(appointmentBySlotId.get(slot.getId()))
                                .map(timeSlotServiceMapper::toAssociatedAppointment)
                                .orElse(null))
                        .build())
                .toList();
    }

    @Override
    public List<AvailableSlot> getAvailability(LocalDate from, LocalDate to) {
        return timeSlotRepository.findBySlotDateBetweenAndStateOrderBySlotDateAscStartTimeAsc(from, to, TimeSlotState.AVAILABLE)
                .stream()
                .filter(ts -> !isInPast(ts.getSlotDate(), ts.getStartTime()))
                .map(timeSlotServiceMapper::toAvailableSlot)
                .toList();
    }

    @Override
    @Transactional
    public List<CreatedSlot> createSlots(CreateSlotsCommand command) {
        var slotDefinitions = command.getSlots();

        rejectDuplicatedIfExist(slotDefinitions);

        slotDefinitions.forEach(slotDefinition -> {
            verifyOnTheHour(slotDefinition.getStartTime());
            verifySlotNotExists(slotDefinition);
            verifyNotInPast(slotDefinition.getDate(), slotDefinition.getStartTime());
        });

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

        slots.forEach(s -> this.verifyNotInPast(s.getSlotDate(), s.getStartTime()));
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
        var slots = timeSlotRepository.findAllByIdForUpdate(ids);
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

    private void verifyOnTheHour(LocalTime startTime) {
        if (startTime.getMinute() != 0 || startTime.getSecond() != 0) {
            throw new InvalidSlotTimeException("Slot must start on the full hour %s".formatted(startTime));
        }
    }

    private void verifySlotNotExists(CreateSlotsCommand.SlotDefinition def) {
        var date = def.getDate();
        var startTime = def.getStartTime();
        if (timeSlotRepository.existsBySlotDateAndStartTime(date, startTime)) {
            throw new SlotConflictException("Slot already exists for %s at %s".formatted(date, startTime));
        }
    }

    private void verifyNotInPast(LocalDate date, LocalTime startTime) {
        if (isInPast(date, startTime)) {
            throw new PastSlotException("Cannot create or publish a slot in the past: %s %s".formatted(date, startTime));
        }
    }

    private void rejectDuplicatedIfExist(List<CreateSlotsCommand.SlotDefinition> slots) {
        var seen = new HashSet<String>();
        slots.forEach(def -> {
            var key = def.getDate() + "T" + def.getStartTime();
            if (!seen.add(key)) {
                throw new SlotConflictException("Duplicate slot in request: %s %s".formatted(def.getDate(), def.getStartTime()) );
            }
        });
    }

    private boolean isInPast(LocalDate date, LocalTime startTime) {
        var now = LocalDateTime.now(clock.withZone(ZoneId.of(brandProperties.getTimezone())));
        return !date.atTime(startTime).isAfter(now);
    }
}

