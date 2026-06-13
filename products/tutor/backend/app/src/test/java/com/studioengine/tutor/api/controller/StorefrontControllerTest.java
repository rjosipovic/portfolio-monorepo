package com.studioengine.tutor.api.controller;

import com.studioengine.tutor.api.dto.ReservationRequest;
import com.studioengine.tutor.api.dto.ReservationResponse;
import com.studioengine.tutor.api.dto.TimeSlotResponse;
import com.studioengine.tutor.scheduling.AvailableSlot;
import com.studioengine.tutor.scheduling.Reservation;
import com.studioengine.tutor.scheduling.ReservationService;
import com.studioengine.tutor.scheduling.TimeSlotService;
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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StorefrontControllerTest {

    @Mock
    private TimeSlotService timeSlotService;

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private StorefrontController storefrontController;

    private MockMvc mockMvc;

    private JacksonTester<ReservationRequest> reservationRequestJson;
    private JacksonTester<ReservationResponse> reservationResponseJson;
    private JacksonTester<List<TimeSlotResponse>> timeSlotResponseJson;

    @BeforeEach
    void setUp() {
        var jsonMapper = JsonMapper.builder().findAndAddModules().build();
        JacksonTester.initFields(this, jsonMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(storefrontController).build();
    }

    // --- GET /availability ---
    @Test
    void shouldReturnAvailableSlots() throws Exception {
        // given
        var slotId = UUID.randomUUID();
        var date = LocalDate.of(2026, 6, 15);
        var startTime = LocalTime.of(10, 0);
        var endTime = LocalTime.of(11, 0);
        var slots = List.of(
                AvailableSlot.builder()
                        .id(slotId)
                        .date(date)
                        .startTime(startTime)
                        .endTime(endTime)
                        .build()
        );
        when(timeSlotService.getAvailability(any(), any())).thenReturn(slots);

        // when
        var response = mockMvc.perform(get("/api/v1/storefront/availability")
                        .param("from", "2026-06-15")
                        .param("to", "2026-06-30"))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        var content = timeSlotResponseJson.parse(response.getContentAsString());
        assertThat(content.getObject()).hasSize(1);
        var slot = content.getObject().getFirst();
        assertThat(slot.getId()).isEqualTo(slotId);
        assertThat(slot.getDate()).isEqualTo(date);
        assertThat(slot.getStartTime()).isEqualTo(startTime);
        assertThat(slot.getEndTime()).isEqualTo(endTime);
    }

    @Test
    void shouldReturnEmptyListWhenNoSlotsAvailable() throws Exception {
        // given
        when(timeSlotService.getAvailability(any(), any())).thenReturn(List.of());

        // when
        var response = mockMvc.perform(get("/api/v1/storefront/availability")
                        .param("from", "2026-06-15")
                        .param("to", "2026-06-30"))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        var content = timeSlotResponseJson.parse(response.getContentAsString());
        assertThat(content.getObject()).isEmpty();
    }

    // --- POST /reservations ---

    @Test
    void shouldReserveSlotAndReturn201() throws Exception {
        // given
        var slotId = UUID.randomUUID();
        var expiresAt = OffsetDateTime.now().plusMinutes(15);
        var reservation = Reservation.builder()
                .timeslotId(slotId)
                .expiresAt(expiresAt)
                .build();

        when(reservationService.reserve(any())).thenReturn(reservation);

        var request = ReservationRequest.builder().timeSlotId(slotId).build();

        // when
        var response = mockMvc.perform(post("/api/v1/storefront/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationRequestJson.write(request).getJson()))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        // then
        var content = reservationResponseJson.parse(response.getContentAsString());
        assertThat(content.getObject().getTimeSlotId()).isEqualTo(slotId);
        assertThat(content.getObject().getExpiresAt()).isNotNull();
    }

    @Test
    void shouldReturn400WhenTimeSlotIdMissing() throws Exception {
        // given
        var json = "{}";

        // when / then
        mockMvc.perform(post("/api/v1/storefront/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}
