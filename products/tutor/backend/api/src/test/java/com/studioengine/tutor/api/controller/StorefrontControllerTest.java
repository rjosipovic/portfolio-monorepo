package com.studioengine.tutor.api.controller;

import com.studioengine.tutor.api.dto.storefront.AppointmentCancellationRequest;
import com.studioengine.tutor.api.dto.storefront.AppointmentCancellationResponse;
import com.studioengine.tutor.api.dto.storefront.AppointmentDetailsResponse;
import com.studioengine.tutor.api.dto.storefront.AppointmentRescheduleRequest;
import com.studioengine.tutor.api.dto.storefront.BrandingConfigurationResponse;
import com.studioengine.tutor.api.dto.storefront.CheckoutRequest;
import com.studioengine.tutor.api.dto.storefront.CheckoutResponse;
import com.studioengine.tutor.api.dto.storefront.RescheduleInitiationResponse;
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
import com.studioengine.tutor.config.BrandProperties;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.dataaccess.enums.TimeSlotState;
import com.studioengine.tutor.errors.ErrorCode;
import com.studioengine.tutor.errors.ErrorResponse;
import com.studioengine.tutor.errors.GlobalExceptionHandler;
import com.studioengine.tutor.errors.exceptions.DeadlinePassedException;
import com.studioengine.tutor.errors.exceptions.InvalidReservationException;
import com.studioengine.tutor.errors.exceptions.PreBookedSelfServiceException;
import com.studioengine.tutor.errors.exceptions.ResourceNotFoundException;
import com.studioengine.tutor.errors.exceptions.SlotConflictException;
import com.studioengine.tutor.errors.exceptions.TokenExpiredException;
import com.studioengine.tutor.scheduling.AvailableSlot;
import com.studioengine.tutor.scheduling.Reservation;
import com.studioengine.tutor.scheduling.ReservationService;
import com.studioengine.tutor.scheduling.ReserveSlotCommand;
import com.studioengine.tutor.scheduling.TimeSlotService;
import com.studioengine.tutor.selfservice.AppointmentCancellation;
import com.studioengine.tutor.selfservice.AppointmentDetails;
import com.studioengine.tutor.selfservice.RescheduleInitiation;
import com.studioengine.tutor.selfservice.SelfServiceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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

import java.time.LocalDate;
import java.time.LocalTime;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

    @Mock
    private BrandProperties brandProperties;

    @Mock
    private SelfServiceManager selfServiceManager;

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
    private JacksonTester<BrandingConfigurationResponse> brandingConfigurationResponseJson;
    private JacksonTester<AppointmentDetailsResponse> appointmentDetailsResponseJson;
    private JacksonTester<AppointmentCancellationRequest> appointmentCancellationRequestJson;
    private JacksonTester<AppointmentCancellationResponse> appointmentCancellationResponseJson;
    private JacksonTester<AppointmentRescheduleRequest> appointmentRescheduleRequestJson;
    private JacksonTester<RescheduleInitiationResponse> rescheduleInitiationResponseJson;

    @BeforeEach
    void setUp() {
        var jsonMapper = JsonMapper.builder().findAndAddModules().build();
        JacksonTester.initFields(this, jsonMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(storefrontController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }


    @Nested
    class ServiceCatalogTests {

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
    }

    @Nested
    class AvailabilityTests {
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
    }

    @Nested
    class ReservationTests {
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
    }

    @Nested
    class CheckoutTests {
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

    @Nested
    class BrandingConfigurationTests {

        @Test
        void shouldReturnBrandProperties() throws Exception {
            // given
            var brandName = "Math Studio";
            var logoUri = "http://logo.uri";
            var primaryColor = "#FF0000";
            var locale = "hr-HR";
            var currency = "EUR";
            var timezone = "Europe/Zagreb";
            when(brandProperties.getName()).thenReturn(brandName);
            when(brandProperties.getLogoUrl()).thenReturn(logoUri);
            when(brandProperties.getPrimaryColor()).thenReturn(primaryColor);
            when(brandProperties.getLocale()).thenReturn(locale);
            when(brandProperties.getCurrency()).thenReturn(currency);
            when(brandProperties.getTimezone()).thenReturn(timezone);

            var expected = BrandingConfigurationResponse.builder()
                    .name(brandName)
                    .logoUrl(logoUri)
                    .primaryColor(primaryColor)
                    .locale(locale)
                    .currency(currency)
                    .timezone(timezone)
                    .build();

            // when
            var response = mockMvc.perform(get("/api/v1/storefront/config/branding"))
                    .andExpect(status().isOk())
                    .andReturn().getResponse();

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContentAsString()).isEqualTo(brandingConfigurationResponseJson.write(expected).getJson());
        }
    }

    @Nested
    class SelfServiceTests {

        @Test
        void shouldValidateToken() throws Exception {
            // given
            var token = UUID.randomUUID().toString();
            var appointmentDetails = mock(AppointmentDetails.class);
            var appointmentDetailsResponse = mock(AppointmentDetailsResponse.class);

            when(appointmentDetailsResponse.getAppointmentId()).thenReturn(UUID.randomUUID());
            when(appointmentDetailsResponse.getStudentName()).thenReturn("Marko Markić");
            when(appointmentDetailsResponse.getServiceCategoryName()).thenReturn("Pripreme za maturu");
            when(appointmentDetailsResponse.getDate()).thenReturn(LocalDate.now().plusDays(1));
            when(appointmentDetailsResponse.getStartTime()).thenReturn(LocalTime.now().plusMinutes(2));
            when(appointmentDetailsResponse.isDeadlineMissed()).thenReturn(false);

            when(selfServiceManager.validateToken(token)).thenReturn(appointmentDetails);
            when(storefrontMapper.toAppointmentDetailsResponse(appointmentDetails)).thenReturn(appointmentDetailsResponse);

            // when
            var response = mockMvc.perform(get("/api/v1/storefront/appointments")
                            .param("token", token))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse();

            // then
            verify(selfServiceManager).validateToken(token);
            verify(storefrontMapper).toAppointmentDetailsResponse(appointmentDetails);

            assertThat(response.getContentAsString()).isEqualTo(appointmentDetailsResponseJson.write(appointmentDetailsResponse).getJson());
        }

        @Test
        void shouldNotValidateWhenTokenNotExist() throws Exception {
            // given
            var token = UUID.randomUUID().toString();

            var reason = "Token %s not found".formatted(token);
            when(selfServiceManager.validateToken(token)).thenThrow(new ResourceNotFoundException(reason));

            var errorResponse = ErrorResponse.builder()
                    .message(ErrorCode.RESOURCE_NOT_FOUND.getMessage())
                    .code(ErrorCode.RESOURCE_NOT_FOUND.getCode())
                    .reason(reason)
                    .build();

            // when
            var response = mockMvc.perform(get("/api/v1/storefront/appointments")
                            .param("token", token))
                    .andExpect(status().isNotFound())
                    .andDo(print())
                    .andReturn().getResponse();

            // then
            verify(selfServiceManager).validateToken(token);

            assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(errorResponse).getJson());
        }

        @Test
        void shouldNotValidateWhenTokenUsed() throws Exception {
            // given
            var token = UUID.randomUUID().toString();

            var reason = "Token already used";
            when(selfServiceManager.validateToken(token)).thenThrow(new TokenExpiredException(reason));

            var errorResponse = ErrorResponse.builder()
                    .message(ErrorCode.TOKEN_EXPIRED.getMessage())
                    .code(ErrorCode.TOKEN_EXPIRED.getCode())
                    .reason(reason)
                    .build();

            // when
            var response = mockMvc.perform(get("/api/v1/storefront/appointments")
                            .param("token", token))
                    .andExpect(status().isGone())
                    .andDo(print())
                    .andReturn().getResponse();

            // then
            verify(selfServiceManager).validateToken(token);

            assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(errorResponse).getJson());
        }

        @Test
        void shouldNotValidateWhenTokenExpired() throws Exception {
            // given
            var token = UUID.randomUUID().toString();

            var reason = "Token expired";
            when(selfServiceManager.validateToken(token)).thenThrow(new TokenExpiredException(reason));

            var errorResponse = ErrorResponse.builder()
                    .message(ErrorCode.TOKEN_EXPIRED.getMessage())
                    .code(ErrorCode.TOKEN_EXPIRED.getCode())
                    .reason(reason)
                    .build();

            // when
            var response = mockMvc.perform(get("/api/v1/storefront/appointments")
                            .param("token", token))
                    .andExpect(status().isGone())
                    .andDo(print())
                    .andReturn().getResponse();

            // then
            verify(selfServiceManager).validateToken(token);

            assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(errorResponse).getJson());
        }

        @Test
        void shouldNotValidateWhenPreBookedToken() throws Exception {
            // given
            var token = UUID.randomUUID().toString();

            var reason = "Self-service not available for direct bookings";
            when(selfServiceManager.validateToken(token)).thenThrow(new PreBookedSelfServiceException(reason));

            var errorResponse = ErrorResponse.builder()
                    .message(ErrorCode.PRE_BOOKED_SELF_SERVICE.getMessage())
                    .code(ErrorCode.PRE_BOOKED_SELF_SERVICE.getCode())
                    .reason(reason)
                    .build();

            // when
            var response = mockMvc.perform(get("/api/v1/storefront/appointments")
                            .param("token", token))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse();

            // then
            verify(selfServiceManager).validateToken(token);

            assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(errorResponse).getJson());
        }

        @Test
        void shouldCancelAppointment() throws Exception {
            // given
            var token = UUID.randomUUID().toString();
            var appointmentCancellationRequest = AppointmentCancellationRequest.builder().token(token).build();
            var appointmentCancellation = mock(AppointmentCancellation.class);
            var appointmentCancellationResponse = mock(AppointmentCancellationResponse.class);

            when(appointmentCancellationResponse.getAppointmentId()).thenReturn(UUID.randomUUID());
            when(appointmentCancellationResponse.getMessage()).thenReturn("Appointment cancelled");
            when(selfServiceManager.confirmCancellation(token)).thenReturn(appointmentCancellation);
            when(storefrontMapper.toAppointmentCancellationResponse(appointmentCancellation)).thenReturn(appointmentCancellationResponse);

            // when
            var response = mockMvc.perform(post("/api/v1/storefront/appointments/cancel")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(appointmentCancellationRequestJson.write(appointmentCancellationRequest).getJson()))
                    .andExpect(status().isOk())
                    .andReturn().getResponse();

            // then
            verify(selfServiceManager).confirmCancellation(token);
            verify(storefrontMapper).toAppointmentCancellationResponse(appointmentCancellation);

            assertThat(response.getContentAsString()).isEqualTo(appointmentCancellationResponseJson.write(appointmentCancellationResponse).getJson());
        }

        @Test
        void shouldNotCancelWhenTokenNotExist() throws Exception {
            // given
            var token = UUID.randomUUID().toString();
            var appointmentCancellationRequest = AppointmentCancellationRequest.builder().token(token).build();

            var reason = "Token %s not found".formatted(token);
            when(selfServiceManager.confirmCancellation(token)).thenThrow(new ResourceNotFoundException(reason));

            var errorResponse = ErrorResponse.builder()
                    .message(ErrorCode.RESOURCE_NOT_FOUND.getMessage())
                    .code(ErrorCode.RESOURCE_NOT_FOUND.getCode())
                    .reason(reason)
                    .build();

            // when
            var response = mockMvc.perform(post("/api/v1/storefront/appointments/cancel")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(appointmentCancellationRequestJson.write(appointmentCancellationRequest).getJson()))
                    .andExpect(status().isNotFound())
                    .andReturn().getResponse();

            // then
            verify(selfServiceManager).confirmCancellation(token);

            assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(errorResponse).getJson());
        }

        @Test
        void shouldNotCancelWhenTokenUsed() throws Exception {
            // given
            var token = UUID.randomUUID().toString();
            var appointmentCancellationRequest = AppointmentCancellationRequest.builder().token(token).build();

            var reason = "Token already used";
            when(selfServiceManager.confirmCancellation(token)).thenThrow(new TokenExpiredException(reason));

            var errorResponse = ErrorResponse.builder()
                    .message(ErrorCode.TOKEN_EXPIRED.getMessage())
                    .code(ErrorCode.TOKEN_EXPIRED.getCode())
                    .reason(reason)
                    .build();

            // when
            var response = mockMvc.perform(post("/api/v1/storefront/appointments/cancel")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(appointmentCancellationRequestJson.write(appointmentCancellationRequest).getJson()))
                    .andExpect(status().isGone())
                    .andReturn().getResponse();

            // then
            verify(selfServiceManager).confirmCancellation(token);

            assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(errorResponse).getJson());
        }

        @Test
        void shouldNotCancelWhenTokenExpired() throws Exception {
            // given
            var token = UUID.randomUUID().toString();
            var appointmentCancellationRequest = AppointmentCancellationRequest.builder().token(token).build();

            var reason = "Token expired";
            when(selfServiceManager.confirmCancellation(token)).thenThrow(new TokenExpiredException(reason));

            var errorResponse = ErrorResponse.builder()
                    .message(ErrorCode.TOKEN_EXPIRED.getMessage())
                    .code(ErrorCode.TOKEN_EXPIRED.getCode())
                    .reason(reason)
                    .build();

            // when
            var response = mockMvc.perform(post("/api/v1/storefront/appointments/cancel")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(appointmentCancellationRequestJson.write(appointmentCancellationRequest).getJson()))
                    .andExpect(status().isGone())
                    .andReturn().getResponse();

            // then
            verify(selfServiceManager).confirmCancellation(token);

            assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(errorResponse).getJson());
        }

        @Test
        void shouldNotCancelWhenPreBookedToken() throws Exception {
            // given
            var token = UUID.randomUUID().toString();
            var appointmentCancellationRequest = AppointmentCancellationRequest.builder().token(token).build();

            var reason = "Self-service not available for direct bookings";
            when(selfServiceManager.confirmCancellation(token)).thenThrow(new PreBookedSelfServiceException(reason));

            var errorResponse = ErrorResponse.builder()
                    .message(ErrorCode.PRE_BOOKED_SELF_SERVICE.getMessage())
                    .code(ErrorCode.PRE_BOOKED_SELF_SERVICE.getCode())
                    .reason(reason)
                    .build();

            // when
            var response = mockMvc.perform(post("/api/v1/storefront/appointments/cancel")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(appointmentCancellationRequestJson.write(appointmentCancellationRequest).getJson()))
                    .andExpect(status().isBadRequest())
                    .andReturn().getResponse();

            // then
            verify(selfServiceManager).confirmCancellation(token);

            assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(errorResponse).getJson());
        }

        @Test
        void shouldNotCancelWhenDeadlinePassed() throws Exception {
            // given
            var token = UUID.randomUUID().toString();
            var appointmentCancellationRequest = AppointmentCancellationRequest.builder().token(token).build();

            var reason = "Cancellation deadline has passed.";
            when(selfServiceManager.confirmCancellation(token)).thenThrow(new DeadlinePassedException(reason));

            var errorResponse = ErrorResponse.builder()
                    .message(ErrorCode.DEADLINE_PASSED.getMessage())
                    .code(ErrorCode.DEADLINE_PASSED.getCode())
                    .reason(reason)
                    .build();

            // when
            var response = mockMvc.perform(post("/api/v1/storefront/appointments/cancel")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(appointmentCancellationRequestJson.write(appointmentCancellationRequest).getJson()))
                    .andExpect(status().isBadRequest())
                    .andReturn().getResponse();

            // then
            verify(selfServiceManager).confirmCancellation(token);

            assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(errorResponse).getJson());
        }

        @Test
        void shouldRescheduleAppointment() throws Exception {
            // given
            var token = UUID.randomUUID().toString();
            var appointmentRescheduleRequest = AppointmentRescheduleRequest.builder().token(token).build();
            var rescheduleInitiation = mock(RescheduleInitiation.class);
            var rescheduleInitiationResponse = mock(RescheduleInitiationResponse.class);

            when(rescheduleInitiationResponse.getOriginalAppointmentId()).thenReturn(UUID.randomUUID());
            when(rescheduleInitiationResponse.getRescheduleToken()).thenReturn(UUID.randomUUID().toString());
            when(rescheduleInitiationResponse.getRedirectUrl()).thenReturn("http://localhost:3000/api/v1/storefront/availability?rescheduleToken=" + UUID.randomUUID());
            when(selfServiceManager.confirmReschedule(token)).thenReturn(rescheduleInitiation);
            when(storefrontMapper.toRescheduleInitiationResponse(rescheduleInitiation)).thenReturn(rescheduleInitiationResponse);

            // when
            var response = mockMvc.perform(post("/api/v1/storefront/appointments/reschedule")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(appointmentRescheduleRequestJson.write(appointmentRescheduleRequest).getJson()))
                    .andExpect(status().isOk())
                    .andReturn().getResponse();

            // then
            verify(selfServiceManager).confirmReschedule(token);
            verify(storefrontMapper).toRescheduleInitiationResponse(rescheduleInitiation);

            assertThat(response.getContentAsString()).isEqualTo(rescheduleInitiationResponseJson.write(rescheduleInitiationResponse).getJson());
        }

        @Test
        void shouldNotRescheduleWhenTokenNotExist() throws Exception {
            // given
            var token = UUID.randomUUID().toString();
            var appointmentRescheduleRequest = AppointmentRescheduleRequest.builder().token(token).build();

            var reason = "Token %s not found".formatted(token);
            when(selfServiceManager.confirmReschedule(token)).thenThrow(new ResourceNotFoundException(reason));

            var errorResponse = ErrorResponse.builder()
                    .message(ErrorCode.RESOURCE_NOT_FOUND.getMessage())
                    .code(ErrorCode.RESOURCE_NOT_FOUND.getCode())
                    .reason(reason)
                    .build();

            // when
            var response = mockMvc.perform(post("/api/v1/storefront/appointments/reschedule")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(appointmentRescheduleRequestJson.write(appointmentRescheduleRequest).getJson()))
                    .andExpect(status().isNotFound())
                    .andReturn().getResponse();

            // then
            verify(selfServiceManager).confirmReschedule(token);

            assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(errorResponse).getJson());
        }

        @Test
        void shouldNotRescheduleWhenTokenUsed() throws Exception {
            // given
            var token = UUID.randomUUID().toString();
            var appointmentRescheduleRequest = AppointmentRescheduleRequest.builder().token(token).build();

            var reason = "Token already used";
            when(selfServiceManager.confirmReschedule(token)).thenThrow(new TokenExpiredException(reason));

            var errorResponse = ErrorResponse.builder()
                    .message(ErrorCode.TOKEN_EXPIRED.getMessage())
                    .code(ErrorCode.TOKEN_EXPIRED.getCode())
                    .reason(reason)
                    .build();

            // when
            var response = mockMvc.perform(post("/api/v1/storefront/appointments/reschedule")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(appointmentRescheduleRequestJson.write(appointmentRescheduleRequest).getJson()))
                    .andExpect(status().isGone())
                    .andReturn().getResponse();

            // then
            verify(selfServiceManager).confirmReschedule(token);

            assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(errorResponse).getJson());
        }

        @Test
        void shouldNotRescheduleWhenTokenExpired() throws Exception {
            // given
            var token = UUID.randomUUID().toString();
            var appointmentRescheduleRequest = AppointmentRescheduleRequest.builder().token(token).build();

            var reason = "Token expired";
            when(selfServiceManager.confirmReschedule(token)).thenThrow(new TokenExpiredException(reason));

            var errorResponse = ErrorResponse.builder()
                    .message(ErrorCode.TOKEN_EXPIRED.getMessage())
                    .code(ErrorCode.TOKEN_EXPIRED.getCode())
                    .reason(reason)
                    .build();

            // when
            var response = mockMvc.perform(post("/api/v1/storefront/appointments/reschedule")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(appointmentRescheduleRequestJson.write(appointmentRescheduleRequest).getJson()))
                    .andExpect(status().isGone())
                    .andReturn().getResponse();

            // then
            verify(selfServiceManager).confirmReschedule(token);

            assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(errorResponse).getJson());
        }

        @Test
        void shouldNotRescheduleWhenPreBookedToken() throws Exception {
            // given
            var token = UUID.randomUUID().toString();
            var appointmentRescheduleRequest = AppointmentRescheduleRequest.builder().token(token).build();

            var reason = "Self-service not available for direct bookings";
            when(selfServiceManager.confirmReschedule(token)).thenThrow(new PreBookedSelfServiceException(reason));

            var errorResponse = ErrorResponse.builder()
                    .message(ErrorCode.PRE_BOOKED_SELF_SERVICE.getMessage())
                    .code(ErrorCode.PRE_BOOKED_SELF_SERVICE.getCode())
                    .reason(reason)
                    .build();

            // when
            var response = mockMvc.perform(post("/api/v1/storefront/appointments/reschedule")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(appointmentRescheduleRequestJson.write(appointmentRescheduleRequest).getJson()))
                    .andExpect(status().isBadRequest())
                    .andReturn().getResponse();

            // then
            verify(selfServiceManager).confirmReschedule(token);

            assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(errorResponse).getJson());
        }

        @Test
        void shouldNotRescheduleWhenDeadLinePassed() throws Exception {
            // given
            var token = UUID.randomUUID().toString();
            var appointmentRescheduleRequest = AppointmentRescheduleRequest.builder().token(token).build();

            var reason = "Cancellation deadline has passed";
            when(selfServiceManager.confirmReschedule(token)).thenThrow(new DeadlinePassedException(reason));

            var errorResponse = ErrorResponse.builder()
                    .message(ErrorCode.DEADLINE_PASSED.getMessage())
                    .code(ErrorCode.DEADLINE_PASSED.getCode())
                    .reason(reason)
                    .build();

            // when
            var response = mockMvc.perform(post("/api/v1/storefront/appointments/reschedule")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(appointmentRescheduleRequestJson.write(appointmentRescheduleRequest).getJson()))
                    .andExpect(status().isBadRequest())
                    .andReturn().getResponse();

            // then
            verify(selfServiceManager).confirmReschedule(token);

            assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(errorResponse).getJson());
        }
    }
}
