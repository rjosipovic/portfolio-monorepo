package com.studioengine.tutor.api.controller;

import com.studioengine.tutor.api.dto.CancelAppointmentRequest;
import com.studioengine.tutor.api.dto.CloseAppointmentRequest;
import com.studioengine.tutor.api.dto.summary.AppointmentSummary;
import com.studioengine.tutor.appointment.AppointmentService;
import com.studioengine.tutor.appointment.CancelAppointmentCommand;
import com.studioengine.tutor.appointment.CloseAppointmentCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard/appointments")
@RequiredArgsConstructor
@Slf4j
public class DashboardAppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/{id}/close")
    public ResponseEntity<AppointmentSummary> close(
            @PathVariable UUID id,
            @Valid @RequestBody CloseAppointmentRequest request
    ) {
        log.info("POST /dashboard/appointments/{}/close outcome={}", id, request.getOutcome());

        var command = CloseAppointmentCommand.builder()
                .appointmentId(id)
                .outcome(mapOutcome(request.getOutcome()))
                .sendFollowup(request.isSendFollowup())
                .build();

        var result = appointmentService.close(command);

        return ResponseEntity.ok(
                AppointmentSummary.builder()
                        .id(result.getAppointmentId())
                        .state(result.getState().name())
                        .build()
        );
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<AppointmentSummary> cancel(
            @PathVariable UUID id,
            @Valid @RequestBody CancelAppointmentRequest request
    ) {
        log.info("POST /dashboard/appointments/{}/cancel", id);

        var command = CancelAppointmentCommand.builder()
                .appointmentId(id)
                .reason(request.getReason())
                .build();

        var result = appointmentService.cancel(command);

        return ResponseEntity.ok(
                AppointmentSummary.builder()
                        .id(result.getAppointmentId())
                        .state(result.getState().name())
                        .build()
                );
    }

    private CloseAppointmentCommand.CloseOutcome mapOutcome(CloseAppointmentRequest.Outcome outcome) {
        return switch (outcome) {
            case COMPLETED -> CloseAppointmentCommand.CloseOutcome.COMPLETED;
            case NO_SHOW -> CloseAppointmentCommand.CloseOutcome.NO_SHOW;
        };
    }
}
