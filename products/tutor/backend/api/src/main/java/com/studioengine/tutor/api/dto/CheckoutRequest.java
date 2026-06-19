package com.studioengine.tutor.api.dto;

import com.studioengine.tutor.checkout.PaymentMethodChoice;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = CheckoutRequest.CheckoutRequestBuilder.class)
public class CheckoutRequest {

    @NotNull
    UUID reservationId;

    @NotNull
    UUID serviceCategoryId;

    @NotNull
    @Valid
    Guest guest;

    String sessionNotes;

    @NotNull
    PaymentMethodChoice paymentMethodChoice;

    @Value
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonDeserialize(builder = Guest.GuestBuilder.class)
    public static class Guest {

        @NotBlank
        String name;

        @NotBlank
        @Email
        String email;

        @NotBlank
        String phone;

        @JsonPOJOBuilder(withPrefix = "")
        public static class GuestBuilder {}
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class CheckoutRequestBuilder {}
}
