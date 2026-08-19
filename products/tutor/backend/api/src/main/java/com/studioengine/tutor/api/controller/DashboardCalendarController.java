package com.studioengine.tutor.api.controller;

import com.studioengine.tutor.api.mapper.CalendarMapper;
import com.studioengine.tutor.api.dto.calendar.CreateSlotsRequest;
import com.studioengine.tutor.api.dto.calendar.DeleteSlotsRequest;
import com.studioengine.tutor.api.dto.calendar.PublishSlotsRequest;
import com.studioengine.tutor.api.dto.calendar.SlotResponse;
import com.studioengine.tutor.api.dto.calendar.WithdrawSlotsRequest;
import com.studioengine.tutor.scheduling.CreateSlotsCommand;
import com.studioengine.tutor.scheduling.DeleteSlotsCommand;
import com.studioengine.tutor.scheduling.PublishSlotsCommand;
import com.studioengine.tutor.scheduling.TimeSlotService;
import com.studioengine.tutor.scheduling.WithdrawSlotsCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard/slots")
@RequiredArgsConstructor
@Slf4j
public class DashboardCalendarController {

    private final TimeSlotService timeSlotService;
    private final CalendarMapper calendarMapper;

    @GetMapping
    public ResponseEntity<List<SlotResponse>> getSlots(
            @RequestParam(required = true, name = "from") LocalDate from,
            @RequestParam(required = true, name = "to") LocalDate to
            ) {
        log.info("GET /dashboard/slots from={} to={}", from, to);
        var result = timeSlotService.getSlotsByDateRange(from, to);
        var response = calendarMapper.toSlotResponses(result);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<List<SlotResponse>> createSlots(@Valid @RequestBody CreateSlotsRequest request) {
        log.info("POST /dashboard/slots count={}", request.getSlots().size());

        var command = CreateSlotsCommand.builder()
                .slots(calendarMapper.toSlotDefinitions(request.getSlots()))
                .build();

        var created = timeSlotService.createSlots(command);

        var response = created.stream()
                .map(calendarMapper::toSlotResponse)
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/publish")
    public ResponseEntity<List<SlotResponse>> publishSlots(@Valid @RequestBody PublishSlotsRequest request) {
        log.info("PATCH /dashboard/slots/publish count={}", request.getSlotIds().size());

        var command = PublishSlotsCommand.builder()
                .slotIds(request.getSlotIds())
                .build();

        var published = timeSlotService.publishSlots(command);

        var response = published.stream()
                .map(calendarMapper::toSlotResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/withdraw")
    public ResponseEntity<Void> withdrawSlot(@Valid @RequestBody WithdrawSlotsRequest request) {
        log.info("PATCH /dashboard/slots/withdraw count={}", request.getSlotIds().size());

        var command = WithdrawSlotsCommand.builder()
                .slotIds(request.getSlotIds())
                        .build();

        timeSlotService.withdrawSlots(command);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteSlots(@Valid @RequestBody DeleteSlotsRequest request) {
        log.info("DELETE /dashboard/slots/batch count={}", request.getSlotIds().size());

        var command = DeleteSlotsCommand.builder()
                .slotIds(request.getSlotIds())
                .build();

        timeSlotService.deleteSlots(command);

        return ResponseEntity.noContent().build();
    }
}