package com.studioengine.tutor.api.controller;

import com.studioengine.tutor.api.dto.CheckoutRequest;
import com.studioengine.tutor.api.dto.CheckoutResponse;
import com.studioengine.tutor.api.dto.ReservationRequest;
import com.studioengine.tutor.api.dto.ReservationResponse;
import com.studioengine.tutor.api.dto.TimeSlotResponse;
import com.studioengine.tutor.checkout.Checkout;
import com.studioengine.tutor.checkout.CheckoutCommand;
import com.studioengine.tutor.checkout.CheckoutService;
import com.studioengine.tutor.scheduling.ReservationService;
import com.studioengine.tutor.scheduling.ReserveSlotCommand;
import com.studioengine.tutor.scheduling.TimeSlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/storefront")
@RequiredArgsConstructor
@Slf4j
public class StorefrontController {

    private final TimeSlotService timeSlotService;
    private final ReservationService reservationService;
    private final CheckoutService checkoutService;

    @GetMapping("/availability")
    public List<TimeSlotResponse> getAvailability(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        log.info("GET /availability from={} to={}", from, to);

        return timeSlotService.getAvailability(from, to)
                .stream()
                .map(slot -> TimeSlotResponse.builder()
                        .id(slot.getId())
                        .date(slot.getDate())
                        .startTime(slot.getStartTime())
                        .endTime(slot.getEndTime())
                        .build())
                .toList();
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> reserve(@Valid @RequestBody ReservationRequest request) {
        log.info("POST /reservations slotId={}", request.getTimeSlotId());

        var command = ReserveSlotCommand.builder()
                .slotId(request.getTimeSlotId())
                .build();
        var result = reservationService.reserve(command);

        var response = ReservationResponse.builder()
                .timeSlotId(result.getTimeslotId())
                .expiresAt(result.getExpiresAt())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        log.info("POST /checkout reservationId={} paymentMethod={}", request.getReservationId(), request.getPaymentMethodChoice());

        var command = CheckoutCommand.builder()
                .reservedSlotId(request.getReservationId())
                .serviceCategoryId(request.getServiceCategoryId())
                .guestName(request.getGuest().getName())
                .guestEmail(request.getGuest().getEmail())
                .guestPhone(request.getGuest().getPhone())
                .sessionNotes(request.getSessionNotes())
                .paymentMethod(request.getPaymentMethodChoice())
                .build();

        var result = checkoutService.checkout(command);

        var response = CheckoutResponse.builder()
                .appointmentId(result.getAppointmentId())
                .status(result.getStatus().name())
                .stripeRedirectUrl(result.getStripeRedirectUrl())
                .message(result.getMessage())
                .benefitApplied(mapBenefit(result.getBenefitApplied()))
                .build();

        return ResponseEntity.ok(response);
    }

    private CheckoutResponse.BenefitAppliedResponse mapBenefit(Checkout.BenefitApplied benefit) {
        if (benefit == null) return null;
        return CheckoutResponse.BenefitAppliedResponse.builder()
                .type(benefit.getType())
                .originalPrice(benefit.getOriginalPrice())
                .finalPrice(benefit.getFinalPrice())
                .build();
    }
}

