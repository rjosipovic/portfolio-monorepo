package com.studioengine.tutor.api.controller;

import com.studioengine.tutor.api.dto.DirectBookingRequest;
import com.studioengine.tutor.api.dto.DirectBookingResponse;
import com.studioengine.tutor.api.dto.summary.AppointmentSummary;
import com.studioengine.tutor.api.dto.summary.ServiceCategorySummary;
import com.studioengine.tutor.api.dto.summary.StudentSummary;
import com.studioengine.tutor.api.dto.summary.TimeSlotSummary;
import com.studioengine.tutor.booking.DirectBookingCommand;
import com.studioengine.tutor.booking.DirectBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard/bookings")
@RequiredArgsConstructor
@Slf4j
public class DashboardBookingController {

    private final DirectBookingService directBookingService;

    @PostMapping("/direct")
    public ResponseEntity<DirectBookingResponse> book(@Valid @RequestBody DirectBookingRequest request) {
        log.info("POST /dashboard/bookings/direct slotId={} studentId={}", request.getTimeSlotId(), request.getStudentId());

        var command = DirectBookingCommand.builder()
                .timeSlotId(request.getTimeSlotId())
                .studentId(request.getStudentId())
                .serviceCategoryId(request.getServiceCategoryId())
                .build();
        var result = directBookingService.book(command);

        var response = DirectBookingResponse.builder()
                .appointment(AppointmentSummary.builder()
                        .id(result.getAppointmentId())
                        .state(result.getState().name())
                        .build())
                .timeSlot(TimeSlotSummary.builder()
                        .id(result.getTimeSlotId())
                        .date(result.getSlotDate())
                        .startTime(result.getStartTime())
                        .build())
                .student(StudentSummary.builder()
                        .id(result.getStudentId())
                        .name(result.getStudentName())
                        .build())
                .serviceCategory(ServiceCategorySummary.builder()
                        .id(result.getServiceCategoryId())
                        .name(result.getServiceCategoryName())
                        .build())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
