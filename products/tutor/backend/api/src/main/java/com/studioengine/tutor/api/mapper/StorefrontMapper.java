package com.studioengine.tutor.api.mapper;

import com.studioengine.tutor.api.dto.storefront.CheckoutRequest;
import com.studioengine.tutor.api.dto.storefront.CheckoutResponse;
import com.studioengine.tutor.api.dto.storefront.ReservationRequest;
import com.studioengine.tutor.api.dto.storefront.ReservationResponse;
import com.studioengine.tutor.api.dto.storefront.ServiceCategoryResponse;
import com.studioengine.tutor.api.dto.storefront.TimeSlotResponse;
import com.studioengine.tutor.catalog.AvailableService;
import com.studioengine.tutor.checkout.Checkout;
import com.studioengine.tutor.checkout.CheckoutCommand;
import com.studioengine.tutor.scheduling.AvailableSlot;
import com.studioengine.tutor.scheduling.Reservation;
import com.studioengine.tutor.scheduling.ReserveSlotCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StorefrontMapper {

    List<ServiceCategoryResponse> toServiceCategoryResponseList(List<AvailableService> availableServices);

    List<TimeSlotResponse> toTimeSlotResponse(List<AvailableSlot> slot);

    @Mapping(source = "timeSlotId", target = "slotId")
    ReserveSlotCommand toReserveSlotCommand(ReservationRequest request);

    ReservationResponse toReservationResponse(Reservation reservation);

    @Mapping(source = "reservationId", target = "reservedSlotId")
    @Mapping(source = "guest.name", target = "guestName")
    @Mapping(source = "guest.email", target = "guestEmail")
    @Mapping(source = "guest.phone", target = "guestPhone")
    @Mapping(source = "paymentMethodChoice", target = "paymentMethod")
    CheckoutCommand toCheckoutCommand(CheckoutRequest request);

    CheckoutResponse toCheckoutResponse(Checkout checkout);

    CheckoutResponse.BenefitAppliedResponse toBenefitResponse(Checkout.BenefitApplied benefit);
}
