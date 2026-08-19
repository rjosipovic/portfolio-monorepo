package com.studioengine.tutor.scheduling;

import com.studioengine.tutor.dataaccess.entities.Appointment;
import com.studioengine.tutor.dataaccess.entities.TimeSlot;
import com.studioengine.tutor.dataaccess.enums.TimeSlotState;
import com.studioengine.tutor.dataaccess.repositories.AppointmentRepository;
import com.studioengine.tutor.dataaccess.repositories.TimeSlotRepository;
import com.studioengine.tutor.dataaccess.repositories.TimeSlotStateLogRepository;
import com.studioengine.tutor.errors.exceptions.ResourceNotFoundException;
import com.studioengine.tutor.errors.exceptions.SlotConflictException;
import com.studioengine.tutor.errors.exceptions.SlotWithdrawalBlockedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeSlotServiceImplTest {

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private TimeSlotStateMachine stateMachine;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private TimeSlotServiceMapper timeSlotServiceMapper;

    @Mock
    private TimeSlotStateLogRepository timeSlotStateLogRepository;

    @InjectMocks
    private TimeSlotServiceImpl timeSlotService;

    @Captor
    private ArgumentCaptor<List<TimeSlot>> slotsCaptor;

    // --- createSlots ---
    @Test
    void shouldCreateSlotsInDraftState() {
        // given
        var date1 = LocalDate.of(2026, 7, 1);
        var time1 = LocalTime.of(10, 0);
        var date2 = LocalDate.of(2026, 7, 1);
        var time2 = LocalTime.of(11, 0);
        var command = CreateSlotsCommand.builder()
                .slots(List.of(
                        CreateSlotsCommand.SlotDefinition.builder().date(date1).startTime(time1).build(),
                        CreateSlotsCommand.SlotDefinition.builder().date(date2).startTime(time2).build()
                ))
                .build();

        var createdSlot = mock(CreatedSlot.class);
        when(timeSlotRepository.existsBySlotDateAndStartTime(date1, time1)).thenReturn(false);
        when(timeSlotRepository.existsBySlotDateAndStartTime(date2, time2)).thenReturn(false);
        when(timeSlotRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        when(timeSlotServiceMapper.toCreatedSlot(any())).thenReturn(createdSlot);

        // when
        timeSlotService.createSlots(command);

        // then
        verify(timeSlotRepository).existsBySlotDateAndStartTime(date1, time1);
        verify(timeSlotRepository).existsBySlotDateAndStartTime(date2, time2);
        verify(timeSlotRepository).saveAll(slotsCaptor.capture());
        verify(timeSlotServiceMapper, times(2)).toCreatedSlot(any());

        var slots = slotsCaptor.getValue();
        assertThat(slots).hasSize(2);
        var firstSlot = slots.getFirst();
        assertThat(firstSlot.getSlotDate()).isEqualTo(date1);
        assertThat(firstSlot.getStartTime()).isEqualTo(time1);
        assertThat(firstSlot.getEndTime()).isEqualTo(time1.plusHours(1));
        assertThat(firstSlot.getState()).isEqualTo(TimeSlotState.DRAFT);

        var secondSlot = slots.getLast();
        assertThat(secondSlot.getSlotDate()).isEqualTo(date2);
        assertThat(secondSlot.getStartTime()).isEqualTo(time2);
        assertThat(secondSlot.getEndTime()).isEqualTo(time2.plusHours(1));
        assertThat(secondSlot.getState()).isEqualTo(TimeSlotState.DRAFT);
    }

    @Test
    void shouldNotCreateSlotsWhenAlreadyExist() {
        // given
        var date1 = LocalDate.of(2026, 7, 1);
        var time1 = LocalTime.of(10, 0);
        var date2 = LocalDate.of(2026, 7, 1);
        var time2 = LocalTime.of(11, 0);
        var command = CreateSlotsCommand.builder()
                .slots(List.of(
                        CreateSlotsCommand.SlotDefinition.builder().date(date1).startTime(time1).build(),
                        CreateSlotsCommand.SlotDefinition.builder().date(date2).startTime(time2).build()
                ))
                .build();
        when(timeSlotRepository.existsBySlotDateAndStartTime(date1, time1)).thenReturn(true);

        // when
        assertThatThrownBy(() -> timeSlotService.createSlots(command))
                .isInstanceOf(SlotConflictException.class);
        // then
        verify(timeSlotRepository).existsBySlotDateAndStartTime(date1, time1);
        verify(timeSlotRepository, never()).existsBySlotDateAndStartTime(date2, time2);
        verify(timeSlotRepository, never()).saveAll(any());
    }

    // --- publishSlots ---
    @Test
    void shouldPublishDraftSlots() {
        // given
        var slot1 = createSlot(TimeSlotState.DRAFT);
        var slot2 = createSlot(TimeSlotState.DRAFT);
        var slots = List.of(slot1, slot2);
        var ids = List.of(slot1.getId(), slot2.getId());
        var command = PublishSlotsCommand.builder().slotIds(ids).build();

        when(timeSlotRepository.findAllById(ids)).thenReturn(slots);
        when(timeSlotRepository.saveAll(slots)).thenAnswer(inv -> inv.getArgument(0));

        // when
        var result = timeSlotService.publishSlots(command);

        // then
        assertThat(result).hasSize(2);
        verify(stateMachine).transition(slot1, TimeSlotState.AVAILABLE, "TUTOR");
        verify(stateMachine).transition(slot2, TimeSlotState.AVAILABLE, "TUTOR");
        verify(timeSlotRepository).saveAll(slots);
    }

    @Test
    void shouldThrowWhenPublishSlotNotFound() {
        // given
        var ids = List.of(UUID.randomUUID(), UUID.randomUUID());
        var command = PublishSlotsCommand.builder().slotIds(ids).build();

        when(timeSlotRepository.findAllById(ids)).thenReturn(List.of(createSlot(TimeSlotState.DRAFT)));

        // when / then
        assertThatThrownBy(() -> timeSlotService.publishSlots(command))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- withdrawSlot ---
    @Test
    void shouldWithdrawAvailableSlot() {
        // given
        var slot1 = createSlot(TimeSlotState.AVAILABLE);
        var slot2 = createSlot(TimeSlotState.AVAILABLE);
        var slots = List.of(slot1, slot2);
        var ids = List.of(slot1.getId(), slot2.getId());
        var command = WithdrawSlotsCommand.builder().slotIds(ids).build();

        when(timeSlotRepository.findAllById(ids)).thenReturn(slots);
        when(appointmentRepository.findByTimeSlotIdInAndStateIn(anyList(), any())).thenReturn(List.of());

        // when
        timeSlotService.withdrawSlots(command);

        // then
        verify(stateMachine).transition(slot1, TimeSlotState.DRAFT, "TUTOR");
        verify(stateMachine).transition(slot2, TimeSlotState.DRAFT, "TUTOR");
        verify(timeSlotRepository).saveAll(slots);
    }

    @Test
    void shouldThrowWhenWithdrawSlotsHasActiveAppointment() {
        // given
        var slot = createSlot(TimeSlotState.AVAILABLE);
        var slots = List.of(slot);
        var ids = List.of(slot.getId());
        var appointment = mock(Appointment.class);
        var command = WithdrawSlotsCommand.builder().slotIds(ids).build();
        when(appointment.getTimeSlot()).thenReturn(slot);

        when(timeSlotRepository.findAllById(ids)).thenReturn(slots);
        when(appointmentRepository.findByTimeSlotIdInAndStateIn(anyList(), any())).thenReturn(List.of(appointment));

        // when / then
        assertThatThrownBy(() -> timeSlotService.withdrawSlots(command))
                .isInstanceOf(SlotWithdrawalBlockedException.class);
        verify(stateMachine, never()).transition(any(), any(), any());
    }

    @Test
    void shouldThrowWhenWithdrawSlotsNotFound() {
        // given
        var slotId = UUID.randomUUID();
        var ids = List.of(slotId);
        var command = WithdrawSlotsCommand.builder().slotIds(ids).build();
        when(timeSlotRepository.findAllById(ids)).thenReturn(List.of());

        // when / then
        assertThatThrownBy(() -> timeSlotService.withdrawSlots(command))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- deleteSlots ---
    @Test
    void shouldDeleteSlotsWithNoActiveAppointments() {
        // given
        var slot1 = createSlot(TimeSlotState.DRAFT);
        var slot2 = createSlot(TimeSlotState.AVAILABLE);
        var ids = List.of(slot1.getId(), slot2.getId());
        var command = DeleteSlotsCommand.builder().slotIds(ids).build();

        when(timeSlotRepository.findAllById(ids)).thenReturn(List.of(slot1, slot2));
        when(appointmentRepository.findByTimeSlotIdInAndStateIn(anyList(), any())).thenReturn(List.of());

        // when
        timeSlotService.deleteSlots(command);

        // then
        verify(timeSlotStateLogRepository).deleteAllByTimeSlotIdIn(ids);
        verify(timeSlotRepository).deleteAll(List.of(slot1, slot2));
    }

    @Test
    void shouldThrowWhenDeleteSlotHasActiveAppointment() {
        // given
        var slot = createSlot(TimeSlotState.AVAILABLE);
        var ids = List.of(slot.getId());
        var command = DeleteSlotsCommand.builder().slotIds(ids).build();
        var appointment = mock(Appointment.class);
        when(appointment.getTimeSlot()).thenReturn(slot);

        when(timeSlotRepository.findAllById(ids)).thenReturn(List.of(slot));
        when(appointmentRepository.findByTimeSlotIdInAndStateIn(anyList(), any())).thenReturn(List.of(appointment));

        // when / then
        assertThatThrownBy(() -> timeSlotService.deleteSlots(command))
                .isInstanceOf(SlotWithdrawalBlockedException.class);
        verify(timeSlotRepository, never()).deleteAll(any());
    }

    // --- getSlotsByDateRange ---
    @Test
    void shouldGetSlotsByDateRange() {
        // given
        var from = LocalDate.of(2026, 8, 18);
        var to = LocalDate.of(2026, 8, 24);
        var slot1 = createSlot(TimeSlotState.DRAFT);
        var slot2 = createSlot(TimeSlotState.AVAILABLE);
        var createdSlot = mock(CreatedSlot.class);

        when(timeSlotRepository.findBySlotDateBetween(from, to)).thenReturn(List.of(slot1, slot2));
        when(timeSlotServiceMapper.toCreatedSlot(any())).thenReturn(createdSlot);

        // when
        var result = timeSlotService.getSlotsByDateRange(from, to);

        // then
        verify(timeSlotRepository).findBySlotDateBetween(from, to);
        verify(timeSlotServiceMapper, times(2)).toCreatedSlot(any());
        assertThat(result).hasSize(2);
    }

    @Test
    void shouldReturnEmptyWhenNoSlotsInDateRange() {
        // given
        var from = LocalDate.of(2026, 8, 18);
        var to = LocalDate.of(2026, 8, 24);

        when(timeSlotRepository.findBySlotDateBetween(from, to)).thenReturn(List.of());

        // when
        var result = timeSlotService.getSlotsByDateRange(from, to);

        // then
        verify(timeSlotRepository).findBySlotDateBetween(from, to);
        verify(timeSlotServiceMapper, never()).toCreatedSlot(any());
        assertThat(result).isEmpty();
    }

    // --- Helper ---

    private TimeSlot createSlot(TimeSlotState state) {
        var slot = TimeSlot.create(LocalDate.of(2026, 7, 1), LocalTime.of(10, 0));
        if (state != TimeSlotState.DRAFT) {
            slot.transitionTo(state);
        }
        setId(slot, UUID.randomUUID());
        return slot;
    }

    private void setId(Object entity, UUID id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}