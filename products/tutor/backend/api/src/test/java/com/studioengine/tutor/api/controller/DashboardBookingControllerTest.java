package com.studioengine.tutor.api.controller;

import com.studioengine.tutor.api.dto.booking.DirectBookingRequest;
import com.studioengine.tutor.api.dto.booking.DirectBookingResponse;
import com.studioengine.tutor.api.mapper.BookingMapper;
import com.studioengine.tutor.booking.DirectBooking;
import com.studioengine.tutor.booking.DirectBookingCommand;
import com.studioengine.tutor.booking.DirectBookingService;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.dataaccess.enums.TimeSlotState;
import com.studioengine.tutor.errors.ErrorCode;
import com.studioengine.tutor.errors.ErrorResponse;
import com.studioengine.tutor.errors.GlobalExceptionHandler;
import com.studioengine.tutor.errors.exceptions.InvalidStateTransitionException;
import com.studioengine.tutor.errors.exceptions.ResourceNotFoundException;
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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DashboardBookingControllerTest {

    @Mock
    private DirectBookingService bookingService;
    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private DashboardBookingController dashboardBookingController;

    private MockMvc mockMvc;

    private JacksonTester<DirectBookingRequest> directBookingRequestJson;
    private JacksonTester<DirectBookingResponse> directBookingResponseJson;
    private JacksonTester<ErrorResponse> errorResponseJson;

    @BeforeEach
    void setUp() {
        var jsonMapper = JsonMapper.builder().findAndAddModules().build();
        JacksonTester.initFields(this, jsonMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardBookingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
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
                .studentEmail("marko.markic@gmail.com")
                .studentPhone("+38599123456")
                .serviceCategoryId(serviceCategoryId)
                .serviceCategoryName("Matematika za osnovnu školu")
                .build();

        var directBookingResponse = mock(DirectBookingResponse.class);

        when(bookingMapper.toCommand(request)).thenReturn(command);
        when(bookingService.book(command)).thenReturn(directBooking);
        when(bookingMapper.toResponse(directBooking)).thenReturn(directBookingResponse);

        // when
        var response = mockMvc.perform(post("/api/v1/dashboard/bookings/direct")
                .contentType(MediaType.APPLICATION_JSON)
                .content(directBookingRequestJson.write(request).getJson()))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        // then
        verify(bookingService).book(command);
        verify(bookingMapper).toCommand(request);
        verify(bookingMapper).toResponse(directBooking);
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

    @Test
    void shouldNotBookWhenTimeSlotNotExists() throws Exception {
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


        when(bookingMapper.toCommand(request)).thenReturn(command);
        var reason = "TimeSlot not found: %s".formatted(timeSlotId.toString());
        when(bookingService.book(command)).thenThrow(new ResourceNotFoundException(reason));
        var expectedError = ErrorResponse.builder()
                .message(ErrorCode.RESOURCE_NOT_FOUND.getMessage())
                .code(ErrorCode.RESOURCE_NOT_FOUND.getCode())
                .reason(reason)
                .build();

        // when
        var response = mockMvc.perform(post("/api/v1/dashboard/bookings/direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(directBookingRequestJson.write(request).getJson()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();

        // then
        verify(bookingService).book(command);
        verify(bookingMapper).toCommand(request);
        assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(expectedError).getJson());
    }

    @Test
    void shouldNotBookWhenInvalidStateTransition() throws Exception {
        // given
        var timeSlotId = UUID.randomUUID();
        var timeSlotState = TimeSlotState.BOOKED;
        var allowedSlotStates = Set.of(TimeSlotState.DRAFT, TimeSlotState.AVAILABLE);
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

        when(bookingMapper.toCommand(request)).thenReturn(command);
        var reason = "Slot %s is in state %s, expected %s".formatted(timeSlotId, timeSlotState, allowedSlotStates);
        when(bookingService.book(command)).thenThrow(new InvalidStateTransitionException(reason));
        var expectedError = ErrorResponse.builder()
                .message(ErrorCode.INVALID_STATE_TRANSITION.getMessage())
                .code(ErrorCode.INVALID_STATE_TRANSITION.getCode())
                .reason(reason)
                .build();

        // when
        var response = mockMvc.perform(post("/api/v1/dashboard/bookings/direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(directBookingRequestJson.write(request).getJson()))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();

        // then
        verify(bookingMapper).toCommand(request);
        verify(bookingService).book(command);
        assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(expectedError).getJson());
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