package com.studioengine.tutor.api.controller;

import com.studioengine.tutor.api.dto.DirectBookingRequest;
import com.studioengine.tutor.api.dto.DirectBookingResponse;
import com.studioengine.tutor.booking.DirectBooking;
import com.studioengine.tutor.booking.DirectBookingCommand;
import com.studioengine.tutor.booking.DirectBookingService;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DashboardBookingControllerTest {

    @Mock
    private DirectBookingService bookingService;

    @InjectMocks
    private DashboardBookingController dashboardBookingController;

    private MockMvc mockMvc;

    private JacksonTester<DirectBookingRequest> directBookingRequestJson;
    private JacksonTester<DirectBookingResponse> directBookingResponseJson;

    @BeforeEach
    void setUp() {
        var jsonMapper = JsonMapper.builder().findAndAddModules().build();
        JacksonTester.initFields(this, jsonMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardBookingController).build();
    }

    // POST /dashboard/bookings
    @Test
    void shouldBook() throws Exception {
        // given
        var timeSlotId = UUID.randomUUID();
        var studentId = UUID.randomUUID();
        var serviceCategoryId = UUID.randomUUID();
        var request = DirectBookingRequest.builder()
                .timeSlotId(timeSlotId)
                .studentId(studentId)
                .serviceCategoryId(serviceCategoryId)
                .build();
        var command = DirectBookingCommand.builder()
                .timeSlotId(request.getTimeSlotId())
                .studentId(request.getStudentId())
                .serviceCategoryId(request.getServiceCategoryId())
                .build();

        var directBooking = DirectBooking.builder()
                .appointmentId(UUID.randomUUID())
                .state(AppointmentState.PRE_BOOKED)
                .timeSlotId(timeSlotId)
                .slotDate(LocalDate.of(2026, 6, 22))
                .startTime(LocalTime.of(10, 0))
                .studentId(studentId)
                .studentName("Marko Markić")
                .serviceCategoryId(serviceCategoryId)
                .serviceCategoryName("Matematika za osnovnu školu")
                .build();

        when(bookingService.book(command)).thenReturn(directBooking);

        // when
        var response = mockMvc.perform(post("/api/v1/dashboard/bookings/direct")
                .contentType(MediaType.APPLICATION_JSON)
                .content(directBookingRequestJson.write(request).getJson()))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        // then
        verify(bookingService).book(command);
        assertThat(response).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("invalidRequests")
    void shouldNotBookWhenMandatoryPropertyMissing(DirectBookingRequest request) throws Exception {
        // given

        // when
        mockMvc.perform(post("/api/v1/dashboard/bookings/direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(directBookingRequestJson.write(request).getJson()))
                .andExpect(status().isBadRequest());

        // then
        verify(bookingService, never()).book(any());
    }

    static Stream<DirectBookingRequest> invalidRequests() {
        var timeSlotId = UUID.randomUUID();
        var studentId = UUID.randomUUID();
        var serviceCategoryId = UUID.randomUUID();
        var missingTimeSlotId = DirectBookingRequest.builder().studentId(studentId).serviceCategoryId(serviceCategoryId).build();
        var missingStudentId = DirectBookingRequest.builder().timeSlotId(timeSlotId).serviceCategoryId(serviceCategoryId).build();
        var missingServiceCategoryId = DirectBookingRequest.builder().timeSlotId(timeSlotId).studentId(studentId).build();
        return Stream.of(missingTimeSlotId, missingStudentId, missingServiceCategoryId);
    }
}