package com.studioengine.tutor.api.controller;

import com.studioengine.tutor.api.dto.storefront.CheckoutRequest;
import com.studioengine.tutor.api.dto.storefront.CheckoutResponse;
import com.studioengine.tutor.api.dto.storefront.ReservationRequest;
import com.studioengine.tutor.api.dto.storefront.ReservationResponse;
import com.studioengine.tutor.api.dto.storefront.ServiceCategoryResponse;
import com.studioengine.tutor.api.dto.storefront.TimeSlotResponse;
import com.studioengine.tutor.api.mapper.StorefrontMapper;
import com.studioengine.tutor.catalog.ServiceCatalog;
import com.studioengine.tutor.checkout.CheckoutService;
import com.studioengine.tutor.scheduling.ReservationService;
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
    private final ServiceCatalog serviceCatalog;
    private final StorefrontMapper storefrontMapper;

    @GetMapping("/services")
    public ResponseEntity<List<ServiceCategoryResponse>> getServices() {
        log.info("GET /storefront/services");

        var result = serviceCatalog.getActiveServices();
        var response = storefrontMapper.toServiceCategoryResponseList(result);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/availability")
    public ResponseEntity<List<TimeSlotResponse>> getAvailability(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        log.info("GET /availability from={} to={}", from, to);

        var result = timeSlotService.getAvailability(from, to);
        var response = storefrontMapper.toTimeSlotResponse(result);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> reserve(@Valid @RequestBody ReservationRequest request) {
        log.info("POST /reservations slotId={}", request.getTimeSlotId());

        var command = storefrontMapper.toReserveSlotCommand(request);
        var result = reservationService.reserve(command);
        var response = storefrontMapper.toReservationResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        log.info("POST /checkout reservationId={} paymentMethod={}", request.getReservationId(), request.getPaymentMethodChoice());

        var command = storefrontMapper.toCheckoutCommand(request);
        var result = checkoutService.checkout(command);
        var response = storefrontMapper.toCheckoutResponse(result);

        return ResponseEntity.ok(response);
    }
}

