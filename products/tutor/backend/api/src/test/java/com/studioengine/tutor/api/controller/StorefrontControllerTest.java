package com.studioengine.tutor.api.controller;

import com.studioengine.tutor.api.dto.CheckoutRequest;
import com.studioengine.tutor.api.dto.CheckoutResponse;
import com.studioengine.tutor.api.dto.ReservationRequest;
import com.studioengine.tutor.api.dto.ReservationResponse;
import com.studioengine.tutor.api.dto.ServiceCategoryResponse;
import com.studioengine.tutor.api.dto.TimeSlotResponse;
import com.studioengine.tutor.catalog.AvailableService;
import com.studioengine.tutor.catalog.ServiceCatalog;
import com.studioengine.tutor.checkout.Checkout;
import com.studioengine.tutor.checkout.CheckoutService;
import com.studioengine.tutor.checkout.PaymentMethodChoice;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.dataaccess.enums.TimeSlotState;
import com.studioengine.tutor.errors.ErrorCode;
import com.studioengine.tutor.errors.ErrorResponse;
import com.studioengine.tutor.errors.GlobalExceptionHandler;
import com.studioengine.tutor.errors.exceptions.InvalidReservationException;
import com.studioengine.tutor.errors.exceptions.ResourceNotFoundException;
import com.studioengine.tutor.errors.exceptions.SlotConflictException;
import com.studioengine.tutor.scheduling.AvailableSlot;
import com.studioengine.tutor.scheduling.Reservation;
import com.studioengine.tutor.scheduling.ReservationService;
import com.studioengine.tutor.scheduling.TimeSlotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StorefrontControllerTest {

    @Mock
    private TimeSlotService timeSlotService;

    @Mock
    private ReservationService reservationService;

    @Mock
    private CheckoutService checkoutService;

    @Mock
    private ServiceCatalog serviceCatalog;

    @InjectMocks
    private StorefrontController storefrontController;

    private MockMvc mockMvc;

    private JacksonTester<ReservationRequest> reservationRequestJson;
    private JacksonTester<ReservationResponse> reservationResponseJson;
    private JacksonTester<List<TimeSlotResponse>> timeSlotResponseJson;
    private JacksonTester<CheckoutRequest> checkoutRequestJson;
    private JacksonTester<CheckoutResponse> checkoutResponseJson;
    private JacksonTester<ErrorResponse> errorResponseJson;
    private JacksonTester<List<ServiceCategoryResponse>> serviceCategoryResponseListJson;

    @BeforeEach
    void setUp() {
        var jsonMapper = JsonMapper.builder().findAndAddModules().build();
        JacksonTester.initFields(this, jsonMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(storefrontController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // GET /services
    @Test
    void shouldReturnActiveServices() throws Exception {
        // given
        var services = List.of(
                AvailableService.builder()
                        .id(UUID.randomUUID())
                        .name("Matematika za osnovnu školu")
                        .description("Pomoć s gradivom od 5. do 8. razreda")
                        .price(new BigDecimal("25.00"))
                        .currency("EUR")
                        .build()
        );
        when(serviceCatalog.getActiveServices()).thenReturn(services);

        // when
        var response = mockMvc.perform(get("/api/v1/storefront/services"))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        var content = serviceCategoryResponseListJson.parse(response.getContentAsString());
        assertThat(content.getObject()).hasSize(1);
    }

    @Test
    void shouldReturnEmptyListWhenNoActiveServices() throws Exception {
        // given
        when(serviceCatalog.getActiveServices()).thenReturn(List.of());

        // when
        var response = mockMvc.perform(get("/api/v1/storefront/services"))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        var content = serviceCategoryResponseListJson.parse(response.getContentAsString());
        assertThat(content.getObject()).isEmpty();
    }

    // --- GET /availability ---
    @Test
    void shouldReturnAvailableSlots() throws Exception {
        // given
        var slotId = UUID.randomUUID();
        var date = LocalDate.of(2026, 6, 15);
        var startTime = LocalTime.of(10, 0);
        var endTime = LocalTime.of(11, 0);
        var slots = List.of(
                AvailableSlot.builder()
                        .id(slotId)
                        .date(date)
                        .startTime(startTime)
                        .endTime(endTime)
                        .build()
        );
        when(timeSlotService.getAvailability(any(), any())).thenReturn(slots);

        // when
        var response = mockMvc.perform(get("/api/v1/storefront/availability")
                        .param("from", "2026-06-15")
                        .param("to", "2026-06-30"))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        var content = timeSlotResponseJson.parse(response.getContentAsString());
        assertThat(content.getObject()).hasSize(1);
        var slot = content.getObject().getFirst();
        assertThat(slot.getId()).isEqualTo(slotId);
        assertThat(slot.getDate()).isEqualTo(date);
        assertThat(slot.getStartTime()).isEqualTo(startTime);
        assertThat(slot.getEndTime()).isEqualTo(endTime);
    }

    @Test
    void shouldReturnEmptyListWhenNoSlotsAvailable() throws Exception {
        // given
        when(timeSlotService.getAvailability(any(), any())).thenReturn(List.of());

        // when
        var response = mockMvc.perform(get("/api/v1/storefront/availability")
                        .param("from", "2026-06-15")
                        .param("to", "2026-06-30"))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        var content = timeSlotResponseJson.parse(response.getContentAsString());
        assertThat(content.getObject()).isEmpty();
    }

    // --- POST /reservations ---
    @Test
    void shouldReserveSlotAndReturn201() throws Exception {
        // given
        var slotId = UUID.randomUUID();
        var expiresAt = OffsetDateTime.now().plusMinutes(15);
        var reservation = Reservation.builder()
                .timeslotId(slotId)
                .expiresAt(expiresAt)
                .build();

        when(reservationService.reserve(any())).thenReturn(reservation);

        var request = ReservationRequest.builder().timeSlotId(slotId).build();

        // when
        var response = mockMvc.perform(post("/api/v1/storefront/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationRequestJson.write(request).getJson()))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        // then
        var content = reservationResponseJson.parse(response.getContentAsString());
        assertThat(content.getObject().getTimeSlotId()).isEqualTo(slotId);
        assertThat(content.getObject().getExpiresAt()).isNotNull();
    }

    @Test
    void shouldReturn400WhenTimeSlotIdMissing() throws Exception {
        // given
        var json = "{}";

        // when / then
        mockMvc.perform(post("/api/v1/storefront/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotReserveWhenTimeSlotNotExists() throws Exception {
        // given
        var slotId = UUID.randomUUID();

        var reason = "TimeSlot not found: %s".formatted(slotId);
        when(reservationService.reserve(any())).thenThrow(new ResourceNotFoundException(reason));

        var request = ReservationRequest.builder().timeSlotId(slotId).build();
        var expectedError = ErrorResponse.builder()
                .message(ErrorCode.RESOURCE_NOT_FOUND.getMessage())
                .code(ErrorCode.RESOURCE_NOT_FOUND.getCode())
                .reason(reason)
                .build();

        // when
        var response = mockMvc.perform(post("/api/v1/storefront/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationRequestJson.write(request).getJson()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();

        // then
        verify(reservationService).reserve(any());
        assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(expectedError).getJson());
    }

    @Test
    void shouldNotReserveWhenSlotConflict() throws Exception {
        // given
        var slotId = UUID.randomUUID();
        var state = AppointmentState.RESERVED;
        var request = ReservationRequest.builder().timeSlotId(slotId).build();

        var reason = "Slot %s is in state %s, expected AVAILABLE".formatted(slotId, state);
        when(reservationService.reserve(any())).thenThrow(new SlotConflictException(reason));

        var expectedError = ErrorResponse.builder()
                .message(ErrorCode.SLOT_CONFLICT.getMessage())
                .code(ErrorCode.SLOT_CONFLICT.getCode())
                .reason(reason)
                .build();

        // when
        var response = mockMvc.perform(post("/api/v1/storefront/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationRequestJson.write(request).getJson()))
                .andExpect(status().isConflict())
                .andReturn().getResponse();

        // then
        verify(reservationService).reserve(any());
        assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(expectedError).getJson());
    }

    // --- POST /checkout ---
    @Test
    void shouldCheckoutWithStripeAndReturn200() throws Exception {
        // given
        var appointmentId = UUID.randomUUID();
        var checkout = Checkout.builder()
                .appointmentId(appointmentId)
                .status(AppointmentState.RESERVED)
                .stripeRedirectUrl("https://checkout.stripe.com/session123")
                .build();

        when(checkoutService.checkout(any())).thenReturn(checkout);

        var request = CheckoutRequest.builder()
                .reservationId(UUID.randomUUID())
                .serviceCategoryId(UUID.randomUUID())
                .guest(CheckoutRequest.Guest.builder()
                        .name("Ana Kovačević")
                        .email("ana@test.com")
                        .phone("+385 91 234 5678")
                        .build())
                .paymentMethodChoice(PaymentMethodChoice.STRIPE)
                .build();

        // when
        var response = mockMvc.perform(post("/api/v1/storefront/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkoutRequestJson.write(request).getJson()))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        var content = checkoutResponseJson.parse(response.getContentAsString());
        assertThat(content.getObject().getAppointmentId()).isEqualTo(appointmentId);
        assertThat(content.getObject().getStatus()).isEqualTo("RESERVED");
        assertThat(content.getObject().getStripeRedirectUrl()).isEqualTo("https://checkout.stripe.com/session123");
    }

    @Test
    void shouldCheckoutWithBankTransferAndReturn200() throws Exception {
        // given
        var appointmentId = UUID.randomUUID();
        var checkout = Checkout.builder()
                .appointmentId(appointmentId)
                .status(AppointmentState.PENDING_PAYMENT)
                .message("Invoice sent to email")
                .build();

        when(checkoutService.checkout(any())).thenReturn(checkout);

        var request = CheckoutRequest.builder()
                .reservationId(UUID.randomUUID())
                .serviceCategoryId(UUID.randomUUID())
                .guest(CheckoutRequest.Guest.builder()
                        .name("Ana Kovačević")
                        .email("ana@test.com")
                        .phone("+385 91 234 5678")
                        .build())
                .paymentMethodChoice(PaymentMethodChoice.BANK_TRANSFER)
                .build();

        // when
        var response = mockMvc.perform(post("/api/v1/storefront/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkoutRequestJson.write(request).getJson()))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        var content = checkoutResponseJson.parse(response.getContentAsString());
        assertThat(content.getObject().getAppointmentId()).isEqualTo(appointmentId);
        assertThat(content.getObject().getStatus()).isEqualTo("PENDING_PAYMENT");
        assertThat(content.getObject().getMessage()).isEqualTo("Invoice sent to email");
        assertThat(content.getObject().getStripeRedirectUrl()).isNull();
    }

    @Test
    void shouldReturn400WhenGuestEmailInvalid() throws Exception {
        // given
        var request = CheckoutRequest.builder()
                .reservationId(UUID.randomUUID())
                .serviceCategoryId(UUID.randomUUID())
                .guest(CheckoutRequest.Guest.builder()
                        .name("Ana")
                        .email("not-an-email")
                        .phone("+385 91 234 5678")
                        .build())
                .paymentMethodChoice(PaymentMethodChoice.STRIPE)
                .build();

        // when / then
        mockMvc.perform(post("/api/v1/storefront/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkoutRequestJson.write(request).getJson()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenGuestMissing() throws Exception {
        // given
        var json = """
              {
                  "reservationId": "%s",
                  "serviceCategoryId": "%s",
                  "paymentMethodChoice": "STRIPE"
              }
              """.formatted(UUID.randomUUID(), UUID.randomUUID());

        // when / then
        mockMvc.perform(post("/api/v1/storefront/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCheckoutWhenInvalidReservation() throws Exception {
        // given
        var slotId = UUID.randomUUID();
        var slotState = TimeSlotState.BOOKED;

        var reason = "Slot %s is in state %s, expected RESERVED".formatted(slotId, slotState);
        when(checkoutService.checkout(any())).thenThrow(new InvalidReservationException(reason));
        var expectedError = ErrorResponse.builder()
                .message(ErrorCode.INVALID_RESERVATION.getMessage())
                .code(ErrorCode.INVALID_RESERVATION.getCode())
                .reason(reason)
                .build();

        var request = CheckoutRequest.builder()
                .reservationId(UUID.randomUUID())
                .serviceCategoryId(UUID.randomUUID())
                .guest(CheckoutRequest.Guest.builder()
                        .name("Ana Kovačević")
                        .email("ana@test.com")
                        .phone("+385 91 234 5678")
                        .build())
                .paymentMethodChoice(PaymentMethodChoice.STRIPE)
                .build();

        // when
        var response = mockMvc.perform(post("/api/v1/storefront/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkoutRequestJson.write(request).getJson()))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();

        // then
        verify(checkoutService).checkout(any());
        assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(expectedError).getJson());
    }
}
