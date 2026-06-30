package com.studioengine.tutor.api.controller;

import com.studioengine.tutor.api.dto.CancelAppointmentRequest;
import com.studioengine.tutor.api.dto.CloseAppointmentRequest;
import com.studioengine.tutor.api.dto.summary.AppointmentSummary;
import com.studioengine.tutor.appointment.AppointmentService;
import com.studioengine.tutor.appointment.CancelAppointmentCommand;
import com.studioengine.tutor.appointment.CanceledAppointment;
import com.studioengine.tutor.appointment.CloseAppointmentCommand;
import com.studioengine.tutor.appointment.ClosedAppointment;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.errors.ErrorCode;
import com.studioengine.tutor.errors.ErrorResponse;
import com.studioengine.tutor.errors.GlobalExceptionHandler;
import com.studioengine.tutor.errors.exceptions.PrematureClosureException;
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

import java.time.LocalDateTime;
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
class DashboardAppointmentControllerTest {

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private DashboardAppointmentController dashboardAppointmentController;

    private MockMvc mockMvc;

    private JacksonTester<CancelAppointmentRequest> cancelAppointmentRequestJson;
    private JacksonTester<CloseAppointmentRequest> closeAppointmentRequestJson;
    private JacksonTester<AppointmentSummary> appointmentSummaryJson;
    private JacksonTester<ErrorResponse> errorResponseJson;

    @BeforeEach
    void setup() {
        var jsonMapper = JsonMapper.builder().findAndAddModules().build();
        JacksonTester.initFields(this, jsonMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardAppointmentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @ParameterizedTest
    @MethodSource("closeOutcomes")
    void shouldClose(CloseAppointmentRequest.Outcome outcome) throws Exception {
        // given
        var appointmentId = UUID.randomUUID();
        var sendFollowup = true;
        var closeRequest = CloseAppointmentRequest.builder()
                .outcome(outcome)
                .sendFollowup(sendFollowup)
                .build();

        var commandOutcome = switch (outcome) {
            case COMPLETED -> CloseAppointmentCommand.CloseOutcome.COMPLETED;
            case NO_SHOW -> CloseAppointmentCommand.CloseOutcome.NO_SHOW;
        };
        var command = CloseAppointmentCommand.builder()
                .appointmentId(appointmentId)
                .outcome(commandOutcome)
                .sendFollowup(sendFollowup)
                .build();
        var closedAppointment = ClosedAppointment.builder()
                .appointmentId(appointmentId)
                .state(AppointmentState.COMPLETED)
                .build();
        when(appointmentService.close(command)).thenReturn(closedAppointment);
        // when
        var response = mockMvc.perform(post("/api/v1/dashboard/appointments/{id}/close", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(closeAppointmentRequestJson.write(closeRequest).getJson()))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        verify(appointmentService).close(command);
        assertThat(response).isNotNull();
    }

    static Stream<CloseAppointmentRequest.Outcome> closeOutcomes() {
        return Stream.of(CloseAppointmentRequest.Outcome.values());
    }

    @Test
    void shouldNotCloseWhenOutcomeIsNull() throws Exception {
        // given
        var appointmentId = UUID.randomUUID();
        var sendFollowup = true;
        var closeRequest = CloseAppointmentRequest.builder()
                .sendFollowup(sendFollowup)
                .build();

        // when
        mockMvc.perform(post("/api/v1/dashboard/appointments/{id}/close", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(closeAppointmentRequestJson.write(closeRequest).getJson()))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();

        // then
        verify(appointmentService, never()).close(any());
    }

    @Test
    void shouldNotCloseWhenAppointmentNotExists() throws Exception {
        // given
        var appointmentId = UUID.randomUUID();
        var sendFollowup = true;
        var closeRequest = CloseAppointmentRequest.builder()
                .outcome(CloseAppointmentRequest.Outcome.COMPLETED)
                .sendFollowup(sendFollowup)
                .build();

        var command = CloseAppointmentCommand.builder()
                .appointmentId(appointmentId)
                .outcome(CloseAppointmentCommand.CloseOutcome.COMPLETED)
                .sendFollowup(sendFollowup)
                .build();

        var reason = "Appointment not found: %s".formatted(appointmentId);
        when(appointmentService.close(command)).thenThrow(new ResourceNotFoundException(reason));
        var expectedError = ErrorResponse.builder()
                .message(ErrorCode.RESOURCE_NOT_FOUND.getMessage())
                .code(ErrorCode.RESOURCE_NOT_FOUND.getCode())
                .reason(reason)
                .build();

        // when
        var response = mockMvc.perform(post("/api/v1/dashboard/appointments/{id}/close", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(closeAppointmentRequestJson.write(closeRequest).getJson()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();

        // then
        verify(appointmentService).close(command);
        assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(expectedError).getJson());
    }

    @Test
    void shouldNotCloseWhenEndTimeNotPassed() throws Exception {
        // given
        var appointmentId = UUID.randomUUID();
        var sendFollowup = true;
        var closeRequest = CloseAppointmentRequest.builder()
                .outcome(CloseAppointmentRequest.Outcome.COMPLETED)
                .sendFollowup(sendFollowup)
                .build();

        var command = CloseAppointmentCommand.builder()
                .appointmentId(appointmentId)
                .outcome(CloseAppointmentCommand.CloseOutcome.COMPLETED)
                .sendFollowup(sendFollowup)
                .build();

        var slotEnd = LocalDateTime.now().plusHours(1);
        var reason = "Cannot close appointment %s - end time %s has not passed".formatted(appointmentId, slotEnd);
        when(appointmentService.close(command)).thenThrow(new PrematureClosureException(reason));
        var expectedError = ErrorResponse.builder()
                .message(ErrorCode.PREMATURE_CLOSURE.getMessage())
                .code(ErrorCode.PREMATURE_CLOSURE.getCode())
                .reason(reason)
                .build();

        // when
        var response = mockMvc.perform(post("/api/v1/dashboard/appointments/{id}/close", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(closeAppointmentRequestJson.write(closeRequest).getJson()))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();

        // then
        verify(appointmentService).close(command);
        assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(expectedError).getJson());
    }

    @Test
    void shouldCancel() throws Exception {
        // given
        var reason = "Need to cancel!";
        var appointmentId = UUID.randomUUID();
        var cancelRequest = CancelAppointmentRequest.builder().reason(reason).build();
        var command = CancelAppointmentCommand.builder().appointmentId(appointmentId).reason(reason).build();
        var canceledAppointment = CanceledAppointment.builder().appointmentId(appointmentId).state(AppointmentState.CANCELLED).build();
        when(appointmentService.cancel(command)).thenReturn(canceledAppointment);

        // when
        var response = mockMvc.perform(post("/api/v1/dashboard/appointments/{id}/cancel", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cancelAppointmentRequestJson.write(cancelRequest).getJson()))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        verify(appointmentService).cancel(command);
        assertThat(response).isNotNull();
    }

    @Test
    void shouldNotCancelWhenReasonMissing() throws Exception {
        var appointmentId = UUID.randomUUID();
        var cancelRequest = CancelAppointmentRequest.builder().build();

        mockMvc.perform(post("/api/v1/dashboard/appointments/{id}/cancel", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cancelAppointmentRequestJson.write(cancelRequest).getJson()))
                .andExpect(status().isBadRequest());

        verify(appointmentService, never()).cancel(any());
    }
}