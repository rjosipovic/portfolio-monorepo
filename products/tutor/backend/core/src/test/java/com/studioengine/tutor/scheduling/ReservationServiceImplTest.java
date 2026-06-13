package com.studioengine.tutor.scheduling;

import com.studioengine.tutor.config.SchedulingProperties;
import com.studioengine.tutor.dataaccess.entities.TimeSlot;
import com.studioengine.tutor.dataaccess.enums.TimeSlotState;
import com.studioengine.tutor.dataaccess.repositories.TimeSlotRepository;
import com.studioengine.tutor.errors.exceptions.ResourceNotFoundException;
import com.studioengine.tutor.errors.exceptions.SlotConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private TimeSlotStateMachine stateMachine;

    @Mock
    private SchedulingProperties schedulingProperties;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    @Test
    void shouldReserveAvailableSlot() {
        // given
        var slot = createAvailableSlot();
        var slotId = slot.getId();
        var command = ReserveSlotCommand.builder().slotId(slotId).build();

        when(timeSlotRepository.findByIdForUpdate(slotId)).thenReturn(Optional.of(slot));
        when(schedulingProperties.getReservationTimeout()).thenReturn(Duration.ofMinutes(15));

        // when
        var result = reservationService.reserve(command);

        // then
        assertThat(result.getTimeslotId()).isEqualTo(slotId);
        assertThat(result.getExpiresAt()).isNotNull();
        verify(stateMachine).transition(slot, TimeSlotState.RESERVED, "GUEST");
        verify(timeSlotRepository).save(slot);
    }

    @Test
    void shouldThrowWhenSlotNotFound() {
        // given
        var slotId = UUID.randomUUID();
        var command = ReserveSlotCommand.builder().slotId(slotId).build();

        when(timeSlotRepository.findByIdForUpdate(slotId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> reservationService.reserve(command))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(stateMachine, never()).transition(any(), any(), any());
        verify(timeSlotRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenSlotNotAvailable() {
        // given
        var slot = createReservedSlot();
        var slotId = slot.getId();
        var command = ReserveSlotCommand.builder().slotId(slotId).build();

        when(timeSlotRepository.findByIdForUpdate(slotId)).thenReturn(Optional.of(slot));

        // when / then
        assertThatThrownBy(() -> reservationService.reserve(command))
                .isInstanceOf(SlotConflictException.class);
        verify(stateMachine, never()).transition(any(), any(), any());
        verify(timeSlotRepository, never()).save(any());
    }

    // --- Helpers ---
    private TimeSlot createAvailableSlot() {
        return createSlotInState(TimeSlotState.AVAILABLE);
    }

    private TimeSlot createReservedSlot() {
        return createSlotInState(TimeSlotState.RESERVED);
    }

    private TimeSlot createSlotInState(TimeSlotState state) {
        var slot = TimeSlot.create(LocalDate.of(2026, 6, 15), LocalTime.of(10, 0));
        if (state != TimeSlotState.DRAFT) {
            slot.transitionTo(state);
        }
        try{
            var idField = TimeSlot.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(slot, UUID.randomUUID());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return slot;
    }
}
