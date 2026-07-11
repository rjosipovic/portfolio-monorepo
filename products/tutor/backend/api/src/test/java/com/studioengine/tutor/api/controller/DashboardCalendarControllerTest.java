package com.studioengine.tutor.api.controller;

import com.studioengine.tutor.api.dto.calendar.CreateSlotsRequest;
import com.studioengine.tutor.api.dto.calendar.DeleteSlotsRequest;
import com.studioengine.tutor.api.dto.calendar.PublishSlotsRequest;
import com.studioengine.tutor.api.dto.calendar.SlotResponse;
import com.studioengine.tutor.api.dto.calendar.WithdrawSlotsRequest;
import com.studioengine.tutor.dataaccess.enums.TimeSlotState;
import com.studioengine.tutor.errors.ErrorCode;
import com.studioengine.tutor.errors.ErrorResponse;
import com.studioengine.tutor.errors.GlobalExceptionHandler;
import com.studioengine.tutor.errors.exceptions.ResourceNotFoundException;
import com.studioengine.tutor.errors.exceptions.SlotConflictException;
import com.studioengine.tutor.errors.exceptions.SlotWithdrawalBlockedException;
import com.studioengine.tutor.scheduling.CreateSlotsCommand;
import com.studioengine.tutor.scheduling.CreatedSlot;
import com.studioengine.tutor.scheduling.DeleteSlotsCommand;
import com.studioengine.tutor.scheduling.PublishSlotsCommand;
import com.studioengine.tutor.scheduling.TimeSlotService;
import com.studioengine.tutor.scheduling.WithdrawSlotsCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DashboardCalendarControllerTest {


    @Mock
    private TimeSlotService timeSlotService;

    @InjectMocks
    private DashboardCalendarController dashboardCalendarController;

    private MockMvc mockMvc;

    private JacksonTester<CreateSlotsRequest> createSlotRequestJson;
    private JacksonTester<List<SlotResponse>> slotResponseListJson;
    private JacksonTester<PublishSlotsRequest> publishSlotRequestJson;
    private JacksonTester<WithdrawSlotsRequest> withdrawSlotsRequestJson;
    private JacksonTester<DeleteSlotsRequest> deleteSlotsRequestJson;
    private JacksonTester<ErrorResponse> errorResponseJson;


    @BeforeEach
    void setUp() {
        var jsonMapper = JsonMapper.builder().findAndAddModules().build();
        JacksonTester.initFields(this, jsonMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardCalendarController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // --- POST /dashboard/slots ---
    @Test
    void shouldCreateSlots() throws Exception {
        // given
        var date1 = LocalDate.of(2026, 6, 22);
        var startTime1 = LocalTime.of(11, 0);
        var slot1 = CreateSlotsRequest.SlotDefinition.builder()
                .date(date1)
                .startTime(startTime1)
                .build();
        var date2 = LocalDate.of(2026, 6, 22);
        var startTime2 = LocalTime.of(12, 0);
        var slot2 = CreateSlotsRequest.SlotDefinition.builder()
                .date(date2)
                .startTime(startTime2)
                .build();
        var slots = List.of(slot1, slot2);
        var request = CreateSlotsRequest.builder()
                .slots(slots)
                .build();
        var command = CreateSlotsCommand.builder()
                .slots(List.of(
                        CreateSlotsCommand.SlotDefinition.builder().date(date1).startTime(startTime1).build(),
                        CreateSlotsCommand.SlotDefinition.builder().date(date2).startTime(startTime2).build()
                ))
                .build();
        var createdSlot1 = CreatedSlot.builder()
                .id(UUID.randomUUID())
                .state(TimeSlotState.DRAFT)
                .date(date1)
                .startTime(startTime1)
                .endTime(startTime1.plusHours(1))
                .build();
        var createdSlot2 = CreatedSlot.builder()
                .id(UUID.randomUUID())
                .state(TimeSlotState.DRAFT)
                .date(date2)
                .startTime(startTime2)
                .endTime(startTime2.plusHours(1))
                .build();
        var createdSlots = List.of(createdSlot1, createdSlot2);

        when(timeSlotService.createSlots(command)).thenReturn(createdSlots);

        // when
        var response = mockMvc.perform(post("/api/v1/dashboard/slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createSlotRequestJson.write(request).getJson()))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        // then
        var content = slotResponseListJson.parse(response.getContentAsString());
        assertThat(content.getObject()).hasSize(2);
        verify(timeSlotService).createSlots(command);
    }

    @Test
    void shouldNotCreateSlotsWhenDateMissing() throws Exception {
        // given
        var startTime = LocalTime.of(11, 0);
        var slot = CreateSlotsRequest.SlotDefinition.builder()
                .startTime(startTime)
                .build();
        var slots = List.of(slot);
        var request = CreateSlotsRequest.builder()
                .slots(slots)
                .build();

        // when
        mockMvc.perform(post("/api/v1/dashboard/slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createSlotRequestJson.write(request).getJson()))
                .andExpect(status().isBadRequest());

        // then
        verify(timeSlotService, never()).createSlots(any());
    }

    @Test
    void shouldNotCreateSlotsWhenStartTimeMissing() throws Exception {
        // given
        var date = LocalDate.of(2026, 6, 22);
        var slot = CreateSlotsRequest.SlotDefinition.builder()
                .date(date)
                .build();
        var slots = List.of(slot);
        var request = CreateSlotsRequest.builder()
                .slots(slots)
                .build();

        // when
        mockMvc.perform(post("/api/v1/dashboard/slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createSlotRequestJson.write(request).getJson()))
                .andExpect(status().isBadRequest());

        // then
        verify(timeSlotService, never()).createSlots(any());
    }

    @Test
    void shouldNotCreateSlotsWhenSLotConflict() throws Exception {
        // given
        var date = LocalDate.of(2026, 6, 22);
        var startTime = LocalTime.of(11, 0);
        var slot = CreateSlotsRequest.SlotDefinition.builder()
                .date(date)
                .startTime(startTime)
                .build();
        var request = CreateSlotsRequest.builder()
                .slots(List.of(slot))
                .build();
        var command = CreateSlotsCommand.builder()
                .slots(List.of(CreateSlotsCommand.SlotDefinition.builder().date(date).startTime(startTime).build()))
                .build();

        var exMsg = "Slot already exists for %s at %s".formatted(date, startTime);
        when(timeSlotService.createSlots(command)).thenThrow(new SlotConflictException(exMsg));
        var expectedError = ErrorResponse.builder()
                .message(ErrorCode.SLOT_CONFLICT.getMessage())
                .code(ErrorCode.SLOT_CONFLICT.getCode())
                .reason(exMsg)
                .build();

        // when
        var response = mockMvc.perform(post("/api/v1/dashboard/slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createSlotRequestJson.write(request).getJson()))
                .andExpect(status().isConflict())
                .andReturn().getResponse();

        // then
        verify(timeSlotService).createSlots(command);
        assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(expectedError).getJson());
    }


    @Test
    void shouldPublishSlots() throws Exception {
        // given
        var slotToPublishId1 = UUID.randomUUID();
        var slotToPublishDate1 = LocalDate.of(2026, 6, 22);
        var slotToPublishStartTime1 = LocalTime.of(10, 0);
        var slotToPublishId2 = UUID.randomUUID();
        var slotToPublishDate2 = LocalDate.of(2026, 6, 22);
        var slotToPublishStartTime2 = LocalTime.of(11, 0);
        var slotIds = List.of(slotToPublishId1, slotToPublishId2);
        var request = PublishSlotsRequest.builder()
                .slotIds(slotIds)
                .build();
        var publishedSlot1 = CreatedSlot.builder()
                .id(slotToPublishId1)
                .state(TimeSlotState.AVAILABLE)
                .date(slotToPublishDate1)
                .startTime(slotToPublishStartTime1)
                .endTime(slotToPublishStartTime1.plusHours(1))
                .build();
        var publishedSlot2 = CreatedSlot.builder()
                .id(slotToPublishId2)
                .state(TimeSlotState.AVAILABLE)
                .date(slotToPublishDate2)
                .startTime(slotToPublishStartTime2)
                .endTime(slotToPublishStartTime2.plusHours(1))
                .build();
        var publishedSlots = List.of(publishedSlot1, publishedSlot2);
        var command = PublishSlotsCommand.builder()
                .slotIds(List.of(slotToPublishId1, slotToPublishId2))
                .build();

        when(timeSlotService.publishSlots(command)).thenReturn(publishedSlots);
        // when
        var response = mockMvc.perform(patch("/api/v1/dashboard/slots/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(publishSlotRequestJson.write(request).getJson()))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        var content = slotResponseListJson.parse(response.getContentAsString());
        assertThat(content.getObject()).hasSize(2);
        verify(timeSlotService).publishSlots(command);
    }

    @Test
    void shouldNotPublishWhenSlotIdsEmpty() throws Exception {
        var request = PublishSlotsRequest.builder().slotIds(List.of()).build();
        mockMvc.perform(patch("/api/v1/dashboard/slots/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(publishSlotRequestJson.write(request).getJson()))
                .andExpect(status().isBadRequest());
        verify(timeSlotService, never()).publishSlots(any());
    }

    @Test
    void shouldNotPublishWhenSlotsNotExist() throws Exception {
        // given
        var slotId = UUID.randomUUID();
        var request = PublishSlotsRequest.builder().slotIds(List.of(slotId)).build();

        when(timeSlotService.publishSlots(any())).thenThrow(new ResourceNotFoundException("One or more TimeSlots not found"));
        var expectedError = ErrorResponse.builder()
                .message(ErrorCode.RESOURCE_NOT_FOUND.getMessage())
                .code(ErrorCode.RESOURCE_NOT_FOUND.getCode())
                .reason("One or more TimeSlots not found")
                .build();
        // when
        var response = mockMvc.perform(patch("/api/v1/dashboard/slots/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(publishSlotRequestJson.write(request).getJson()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();

        // then
        verify(timeSlotService).publishSlots(any());
        assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(expectedError).getJson());
    }

    @Test
    void shouldWithdrawSlot() throws Exception {
        // given
        var slotToWithdrawId1 = UUID.randomUUID();
        var slotToWithdrawId2 = UUID.randomUUID();
        var slotIds = List.of(slotToWithdrawId1, slotToWithdrawId2);
        var request = WithdrawSlotsRequest.builder()
                .slotIds(slotIds)
                .build();

        var command = WithdrawSlotsCommand.builder()
                .slotIds(List.of(slotToWithdrawId1, slotToWithdrawId2))
                .build();

        // when
        mockMvc.perform(patch("/api/v1/dashboard/slots/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(withdrawSlotsRequestJson.write(request).getJson()))
                .andExpect(status().isNoContent());

        // then
        verify(timeSlotService).withdrawSlots(command);

    }

    @Test
    void shouldNotWithdrawWhenSlotIdsEmpty() throws Exception {
        var request = WithdrawSlotsRequest.builder().slotIds(List.of()).build();
        mockMvc.perform(patch("/api/v1/dashboard/slots/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(withdrawSlotsRequestJson.write(request).getJson()))
                .andExpect(status().isBadRequest());
        verify(timeSlotService, never()).withdrawSlots(any());
    }

    @Test
    void shouldNotWithdrawSlotWhenBlocked() throws Exception {
        // given
        var slotToWithdrawId1 = UUID.randomUUID();
        var slotToWithdrawId2 = UUID.randomUUID();
        var slotIds = List.of(slotToWithdrawId1, slotToWithdrawId2);
        var request = WithdrawSlotsRequest.builder()
                .slotIds(slotIds)
                .build();

        var command = WithdrawSlotsCommand.builder()
                .slotIds(List.of(slotToWithdrawId1, slotToWithdrawId2))
                .build();
        doThrow(new SlotWithdrawalBlockedException("Slot %s has an active appointment".formatted(slotToWithdrawId2))).when(timeSlotService).withdrawSlots(command);
        var expectedError = ErrorResponse.builder()
                .message(ErrorCode.SLOT_WITHDRAWAL_BLOCKED.getMessage())
                .code(ErrorCode.SLOT_WITHDRAWAL_BLOCKED.getCode())
                .reason("Slot %s has an active appointment".formatted(slotToWithdrawId2))
                .build();

        // when
        var response = mockMvc.perform(patch("/api/v1/dashboard/slots/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(withdrawSlotsRequestJson.write(request).getJson()))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();

        // then
        verify(timeSlotService).withdrawSlots(command);
        assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(expectedError).getJson());
    }

    @Test
    void shouldDeleteSlots() throws Exception {
        // given
        var slotToDeleteId1 = UUID.randomUUID();
        var slotToDeleteId2 = UUID.randomUUID();
        var slotIds = List.of(slotToDeleteId1, slotToDeleteId2);
        var request = DeleteSlotsRequest.builder().slotIds(slotIds).build();

        var command = DeleteSlotsCommand.builder()
                .slotIds(slotIds)
                        .build();

        // when
        mockMvc.perform(delete("/api/v1/dashboard/slots")
                .contentType(MediaType.APPLICATION_JSON)
                .content(deleteSlotsRequestJson.write(request).getJson()))
                .andExpect(status().isNoContent());

        // then
        verify(timeSlotService).deleteSlots(command);
    }

    @Test
    void shouldNotDeleteWhenSlotIdsEmpty() throws Exception {
        var request = DeleteSlotsRequest.builder().slotIds(List.of()).build();
        mockMvc.perform(delete("/api/v1/dashboard/slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deleteSlotsRequestJson.write(request).getJson()))
                .andExpect(status().isBadRequest());
        verify(timeSlotService, never()).deleteSlots(any());
    }
}