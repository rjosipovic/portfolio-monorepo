package com.studioengine.tutor.api.controller;

import com.studioengine.tutor.api.dto.booking.DirectBookingRequest;
import com.studioengine.tutor.api.dto.booking.DirectBookingResponse;
import com.studioengine.tutor.api.mapper.BookingMapper;
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
    private final BookingMapper bookingMapper;

    @PostMapping("/direct")
    public ResponseEntity<DirectBookingResponse> book(@Valid @RequestBody DirectBookingRequest request) {
        log.info("POST /dashboard/bookings/direct slotId={} studentId={}", request.getTimeSlotId(), request.getStudentId());

        var command = bookingMapper.toCommand(request);
        var result = directBookingService.book(command);
        var response = bookingMapper.toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
