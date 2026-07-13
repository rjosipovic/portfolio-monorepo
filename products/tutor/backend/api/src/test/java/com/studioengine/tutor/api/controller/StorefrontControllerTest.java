package com.studioengine.tutor.api.controller;

import com.studioengine.tutor.api.dto.storefront.CheckoutRequest;
import com.studioengine.tutor.api.dto.storefront.CheckoutResponse;
import com.studioengine.tutor.api.dto.storefront.ReservationRequest;
import com.studioengine.tutor.api.dto.storefront.ReservationResponse;
import com.studioengine.tutor.api.dto.storefront.ServiceCategoryResponse;
import com.studioengine.tutor.api.dto.storefront.TimeSlotResponse;
import com.studioengine.tutor.api.mapper.StorefrontMapper;
import com.studioengine.tutor.catalog.AvailableService;
import com.studioengine.tutor.catalog.ServiceCatalog;
import com.studioengine.tutor.checkout.Checkout;
import com.studioengine.tutor.checkout.CheckoutCommand;
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
import com.studioengine.tutor.scheduling.ReserveSlotCommand;
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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

    @Mock
    private StorefrontMapper storefrontMapper;

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
        var availableService = mock(AvailableService.class);
        var serviceCategoryResponse = mock(ServiceCategoryResponse.class);
        when(serviceCatalog.getActiveServices()).thenReturn(List.of(availableService));
        when(storefrontMapper.toServiceCategoryResponseList(List.of(availableService))).thenReturn(List.of(serviceCategoryResponse));

        // when
        var response = mockMvc.perform(get("/api/v1/storefront/services"))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        verify(serviceCatalog).getActiveServices();
        verify(storefrontMapper).toServiceCategoryResponseList(List.of(availableService));
        var content = serviceCategoryResponseListJson.parse(response.getContentAsString());
        assertThat(content.getObject()).hasSize(1);
    }

    @Test
    void shouldReturnEmptyListWhenNoActiveServices() throws Exception {
        // given
        when(serviceCatalog.getActiveServices()).thenReturn(List.of());
        when(storefrontMapper.toServiceCategoryResponseList(List.of())).thenReturn(List.of());

        // when
        var response = mockMvc.perform(get("/api/v1/storefront/services"))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        verify(serviceCatalog).getActiveServices();
        verify(storefrontMapper).toServiceCategoryResponseList(List.of());
        var content = serviceCategoryResponseListJson.parse(response.getContentAsString());
        assertThat(content.getObject()).isEmpty();
    }

    // --- GET /availability ---
    @Test
    void shouldReturnAvailableSlots() throws Exception {
        // given
        var slot = mock(AvailableSlot.class);

        var timeSlotResponse = mock(TimeSlotResponse.class);
        when(timeSlotService.getAvailability(any(), any())).thenReturn(List.of(slot));
        when(storefrontMapper.toTimeSlotResponse(List.of(slot))).thenReturn(List.of(timeSlotResponse));

        // when
        var response = mockMvc.perform(get("/api/v1/storefront/availability")
                        .param("from", "2026-06-15")
                        .param("to", "2026-06-30"))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        verify(timeSlotService).getAvailability(any(), any());
        verify(storefrontMapper).toTimeSlotResponse(List.of(slot));

        var content = timeSlotResponseJson.parse(response.getContentAsString());
        assertThat(content.getObject()).hasSize(1);
    }

    @Test
    void shouldReturnEmptyListWhenNoSlotsAvailable() throws Exception {
        // given
        when(timeSlotService.getAvailability(any(), any())).thenReturn(List.of());
        when(storefrontMapper.toTimeSlotResponse(List.of())).thenReturn(List.of());

        // when
        var response = mockMvc.perform(get("/api/v1/storefront/availability")
                        .param("from", "2026-06-15")
                        .param("to", "2026-06-30"))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        verify(timeSlotService).getAvailability(any(), any());
        verify(storefrontMapper).toTimeSlotResponse(List.of());
        var content = timeSlotResponseJson.parse(response.getContentAsString());
        assertThat(content.getObject()).isEmpty();
    }

    // --- POST /reservations ---
    @Test
    void shouldReserveSlotAndReturn201() throws Exception {
        // given
        var slotId = UUID.randomUUID();

        var request = ReservationRequest.builder().timeSlotId(slotId).build();
        var command = mock(ReserveSlotCommand.class);
        var reservation = mock(Reservation.class);
        var reservationResponse = mock(ReservationResponse.class);

        when(storefrontMapper.toReserveSlotCommand(request)).thenReturn(command);
        when(reservationService.reserve(command)).thenReturn(reservation);
        when(storefrontMapper.toReservationResponse(reservation)).thenReturn(reservationResponse);

        // when
        var response = mockMvc.perform(post("/api/v1/storefront/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationRequestJson.write(request).getJson()))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        // then
        verify(storefrontMapper).toReserveSlotCommand(request);
        verify(reservationService).reserve(command);
        verify(storefrontMapper).toReservationResponse(reservation);
        var content = reservationResponseJson.parse(response.getContentAsString());
        assertThat(content.getObject()).isNotNull();
    }

    @Test
    void shouldReturn400WhenTimeSlotIdMissing() throws Exception {
        // given
        var json = "{}";

        // when
        mockMvc.perform(post("/api/v1/storefront/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        // then
        verify(storefrontMapper, never()).toReserveSlotCommand(any());
        verify(reservationService, never()).reserve(any());
        verify(storefrontMapper, never()).toReservationResponse(any());
    }

    @Test
    void shouldNotReserveWhenTimeSlotNotExists() throws Exception {
        // given
        var slotId = UUID.randomUUID();
        var request = ReservationRequest.builder().timeSlotId(slotId).build();
        var command = mock(ReserveSlotCommand.class);

        when(storefrontMapper.toReserveSlotCommand(request)).thenReturn(command);
        var reason = "TimeSlot not found: %s".formatted(slotId);
        when(reservationService.reserve(command)).thenThrow(new ResourceNotFoundException(reason));

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
        verify(storefrontMapper).toReserveSlotCommand(request);
        verify(reservationService).reserve(any());
        verify(storefrontMapper, never()).toReservationResponse(any());
        assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(expectedError).getJson());
    }

    @Test
    void shouldNotReserveWhenSlotConflict() throws Exception {
        // given
        var slotId = UUID.randomUUID();
        var state = AppointmentState.RESERVED;
        var request = ReservationRequest.builder().timeSlotId(slotId).build();
        var command = mock(ReserveSlotCommand.class);

        when(storefrontMapper.toReserveSlotCommand(request)).thenReturn(command);
        var reason = "Slot %s is in state %s, expected AVAILABLE".formatted(slotId, state);
        when(reservationService.reserve(command)).thenThrow(new SlotConflictException(reason));

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
        verify(storefrontMapper).toReserveSlotCommand(request);
        verify(reservationService).reserve(command);
        verify(storefrontMapper, never()).toReservationResponse(any());
        assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(expectedError).getJson());
    }

    // --- POST /checkout ---
    @Test
    void shouldCheckout() throws Exception {
        // given
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
        var command = mock(CheckoutCommand.class);
        var checkout = mock(Checkout.class);
        var checkoutResponse = mock(CheckoutResponse.class);

        when(storefrontMapper.toCheckoutCommand(request)).thenReturn(command);
        when(checkoutService.checkout(command)).thenReturn(checkout);
        when(storefrontMapper.toCheckoutResponse(checkout)).thenReturn(checkoutResponse);

        // when
        var response = mockMvc.perform(post("/api/v1/storefront/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkoutRequestJson.write(request).getJson()))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        verify(storefrontMapper).toCheckoutCommand(request);
        verify(checkoutService).checkout(command);
        verify(storefrontMapper).toCheckoutResponse(checkout);
        var content = checkoutResponseJson.parse(response.getContentAsString());
        assertThat(content.getObject()).isNotNull();
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

        // when
        mockMvc.perform(post("/api/v1/storefront/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkoutRequestJson.write(request).getJson()))
                .andExpect(status().isBadRequest());

        // then
        verify(storefrontMapper, never()).toCheckoutCommand(any());
        verify(checkoutService, never()).checkout(any());
        verify(storefrontMapper, never()).toCheckoutResponse(any());
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

        // when
        mockMvc.perform(post("/api/v1/storefront/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        // then
        verify(storefrontMapper, never()).toCheckoutCommand(any());
        verify(checkoutService, never()).checkout(any());
        verify(storefrontMapper, never()).toCheckoutResponse(any());
    }

    @Test
    void shouldNotCheckoutWhenInvalidReservation() throws Exception {
        // given
        var slotId = UUID.randomUUID();
        var slotState = TimeSlotState.BOOKED;

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
        var command = mock(CheckoutCommand.class);

        when(storefrontMapper.toCheckoutCommand(request)).thenReturn(command);
        var reason = "Slot %s is in state %s, expected RESERVED".formatted(slotId, slotState);
        when(checkoutService.checkout(any())).thenThrow(new InvalidReservationException(reason));
        var expectedError = ErrorResponse.builder()
                .message(ErrorCode.INVALID_RESERVATION.getMessage())
                .code(ErrorCode.INVALID_RESERVATION.getCode())
                .reason(reason)
                .build();


        // when
        var response = mockMvc.perform(post("/api/v1/storefront/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkoutRequestJson.write(request).getJson()))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();

        // then
        verify(storefrontMapper).toCheckoutCommand(request);
        verify(checkoutService).checkout(command);
        verify(storefrontMapper, never()).toCheckoutResponse(any());
        assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(expectedError).getJson());
    }
}
