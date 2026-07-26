package com.studioengine.tutor.api.controller;

import com.studioengine.tutor.api.dto.finance.ConfirmedPaymentResponse;
import com.studioengine.tutor.api.dto.finance.MonthlyRevenueResponse;
import com.studioengine.tutor.api.dto.finance.PendingPaymentResponse;
import com.studioengine.tutor.api.mapper.FinanceMapper;
import com.studioengine.tutor.dataaccess.enums.AppointmentState;
import com.studioengine.tutor.errors.ErrorCode;
import com.studioengine.tutor.errors.ErrorResponse;
import com.studioengine.tutor.errors.GlobalExceptionHandler;
import com.studioengine.tutor.errors.exceptions.InvalidStateTransitionException;
import com.studioengine.tutor.errors.exceptions.ResourceNotFoundException;
import com.studioengine.tutor.finance.ConfirmBankTransferCommand;
import com.studioengine.tutor.finance.ConfirmedPayment;
import com.studioengine.tutor.finance.FinanceService;
import com.studioengine.tutor.finance.MonthlyRevenue;
import com.studioengine.tutor.finance.MonthlyRevenueQuery;
import com.studioengine.tutor.finance.PendingPayment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
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
class DashboardFinanceControllerTest {

    @Mock
    private FinanceService financeService;
    @Mock
    private FinanceMapper financeMapper;
    @InjectMocks
    private DashboardFinanceController dashboardFinanceController;

    private MockMvc mockMvc;

    private JacksonTester<ConfirmedPaymentResponse> confirmedPaymentResponseJson;
    private JacksonTester<MonthlyRevenueResponse> monthlyRevenueResponseJson;
    private JacksonTester<List<PendingPaymentResponse>> pendingPaymentResponseListJson;
    private JacksonTester<ErrorResponse> errorResponseJson;

    @BeforeEach
    void setUp() {
        var jsonMapper = JsonMapper.builder().findAndAddModules().build();
        JacksonTester.initFields(this, jsonMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardFinanceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    class MonthlyRevenueTests {

        @Test
        void shouldGetMonthlyRevenue() throws Exception {
            // given
            var year = 2026;
            var month = 6;
            var totalRevenue = BigDecimal.valueOf(100);
            var stripeRevenue = BigDecimal.valueOf(20);
            var bankTransferRevenue = BigDecimal.valueOf(30);
            var cashRevenue = BigDecimal.valueOf(50);
            var billableHours = 7;
            var completedAppointments = 7;

            var monthlyRevenue = mock(MonthlyRevenue.class);
            var monthlyRevenueResponse = mock(MonthlyRevenueResponse.class);
            when(monthlyRevenueResponse.getYear()).thenReturn(year);
            when(monthlyRevenueResponse.getMonth()).thenReturn(month);
            when(monthlyRevenueResponse.getTotalRevenue()).thenReturn(totalRevenue);
            when(monthlyRevenueResponse.getStripePayments()).thenReturn(stripeRevenue);
            when(monthlyRevenueResponse.getBankTransferPayments()).thenReturn(bankTransferRevenue);
            when(monthlyRevenueResponse.getCashPayments()).thenReturn(cashRevenue);
            when(monthlyRevenueResponse.getBillableHours()).thenReturn(billableHours);
            when(monthlyRevenueResponse.getCompletedAppointments()).thenReturn(completedAppointments);
            when(financeService.getMonthlyRevenue(any(MonthlyRevenueQuery.class))).thenReturn(monthlyRevenue);
            when(financeMapper.toMonthlyRevenueResponse(monthlyRevenue)).thenReturn(monthlyRevenueResponse);

            // when
            var response = mockMvc.perform(get("/api/v1/dashboard/finance/monthly")
                    .queryParam("year", String.valueOf(year))
                    .queryParam("month", String.valueOf(month)))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse();
            // then
            var captor = ArgumentCaptor.forClass(MonthlyRevenueQuery.class);
            verify(financeService).getMonthlyRevenue(captor.capture());
            var query = captor.getValue();
            assertThat(query.getYear()).isEqualTo(year);
            assertThat(query.getMonth()).isEqualTo(month);

            verify(financeMapper).toMonthlyRevenueResponse(monthlyRevenue);

            assertThat(response).isNotNull();
            assertThat(response.getContentAsString()).isEqualTo(monthlyRevenueResponseJson.write(monthlyRevenueResponse).getJson());
        }

        @Test
        void shouldNotGetMonthlyRevenueWhenYearMissing() throws Exception {
            // given
            var month = 6;

            var errorResponse = ErrorResponse.builder()
                    .message("Missing required parameter")
                    .code("VALIDATION")
                    .reason("Parameter 'year' is required")
                    .build();
            // when
            var response = mockMvc.perform(get("/api/v1/dashboard/finance/monthly")
                            .queryParam("month", String.valueOf(month)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse();
            // then
            verify(financeMapper, never()).toMonthlyRevenueResponse(any());
            assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(errorResponse).getJson());
        }

        @Test
        void shouldNotGetMonthlyRevenueWhenMonthMissing() throws Exception {
            // given
            var year = 2026;

            var errorResponse = ErrorResponse.builder()
                    .message("Missing required parameter")
                    .code("VALIDATION")
                    .reason("Parameter 'month' is required")
                    .build();
            // when
            var response = mockMvc.perform(get("/api/v1/dashboard/finance/monthly")
                            .queryParam("year", String.valueOf(year)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse();
            // then
            verify(financeMapper, never()).toMonthlyRevenueResponse(any());
            assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(errorResponse).getJson());
        }

        @Test
        void shouldNotGetMonthlyRevenueWhenYearMonthNotNumber() throws Exception {
            // given
            var month = "six";
            var year = "two thousand and twenty six";

            var errorResponse = ErrorResponse.builder()
                    .message("Invalid parameter type")
                    .code("VALIDATION")
                    .reason("Parameter 'year' must be of type Integer")
                    .build();
            // when
            var response = mockMvc.perform(get("/api/v1/dashboard/finance/monthly")
                            .queryParam("month", month)
                            .queryParam("year", year))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse();
            // then
            verify(financeMapper, never()).toMonthlyRevenueResponse(any());
            assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(errorResponse).getJson());
        }
    }

    @Nested
    class PendingPaymentsTests {

        @Test
        void shouldReturnPendingPayments() throws Exception {
            // given
            var appointmentId = UUID.randomUUID();
            var studentName = "Marko Markić";
            var amount = BigDecimal.TEN;
            var createdAt = OffsetDateTime.now().minusDays(1);
            var pendingPayment = mock(PendingPayment.class);
            var pendingPaymentResponse = mock(PendingPaymentResponse.class);
            when(pendingPaymentResponse.getAppointmentId()).thenReturn(appointmentId);
            when(pendingPaymentResponse.getStudentName()).thenReturn(studentName);
            when(pendingPaymentResponse.getAmount()).thenReturn(amount);
            when(pendingPaymentResponse.getCreatedAt()).thenReturn(createdAt);
            when(financeService.getPendingPayments()).thenReturn(List.of(pendingPayment));
            when(financeMapper.toPendingPaymentResponseList(List.of(pendingPayment))).thenReturn(List.of(pendingPaymentResponse));

            // when
            var response = mockMvc.perform(get("/api/v1/dashboard/finance/pending"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse();

            // then
            verify(financeService).getPendingPayments();
            verify(financeMapper).toPendingPaymentResponseList(List.of(pendingPayment));
            assertThat(response).isNotNull();
            assertThat(response.getContentAsString()).isEqualTo(pendingPaymentResponseListJson.write(List.of(pendingPaymentResponse)).getJson());
        }

        @Test
        void shouldReturnEmptyPendingPayments() throws Exception {
            // given
            when(financeService.getPendingPayments()).thenReturn(List.of());
            when(financeMapper.toPendingPaymentResponseList(List.of())).thenReturn(List.of());

            // when
            var response = mockMvc.perform(get("/api/v1/dashboard/finance/pending"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse();

            // then
            verify(financeService).getPendingPayments();
            verify(financeMapper).toPendingPaymentResponseList(List.of());
            assertThat(response).isNotNull();
            assertThat(response.getContentAsString()).isEqualTo(pendingPaymentResponseListJson.write(List.of()).getJson());
        }
    }

    @Nested
    class ConfirmPaymentTests {

        @Test
        void shouldConfirmBankTransfer() throws Exception {
            // given
            var appointmentId = UUID.randomUUID();
            var confirmedPayment = mock(ConfirmedPayment.class);
            var confirmedPaymentResponse = mock(ConfirmedPaymentResponse.class);
            var state = AppointmentState.CONFIRMED.name();
            var confirmedAt = OffsetDateTime.now().minusSeconds(1);
            when(confirmedPaymentResponse.getAppointmentId()).thenReturn(appointmentId);
            when(confirmedPaymentResponse.getState()).thenReturn(state);
            when(confirmedPaymentResponse.getConfirmedAt()).thenReturn(confirmedAt);
            when(financeService.confirmBankTransfer(any(ConfirmBankTransferCommand.class))).thenReturn(confirmedPayment);
            when(financeMapper.toConfirmedPaymentResponse(confirmedPayment)).thenReturn(confirmedPaymentResponse);

            // when
            var response = mockMvc.perform(post("/api/v1/dashboard/finance/confirm-bank-transfer/{appointmentId}", appointmentId))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse();

            // then
            var captor = ArgumentCaptor.forClass(ConfirmBankTransferCommand.class);
            verify(financeService).confirmBankTransfer(captor.capture());
            assertThat(captor.getValue().getAppointmentId()).isEqualTo(appointmentId);

            verify(financeMapper).toConfirmedPaymentResponse(confirmedPayment);
            assertThat(response).isNotNull();
            assertThat(response.getContentAsString()).isEqualTo(confirmedPaymentResponseJson.write(confirmedPaymentResponse).getJson());
        }

        @Test
        void shouldNotConfirmBankTransferWhenAppointmentNotExists() throws Exception {
            // given
            var appointmentId = UUID.randomUUID();
            var reason = "Appointment not found: %s".formatted(appointmentId);
            when(financeService.confirmBankTransfer(any(ConfirmBankTransferCommand.class))).thenThrow(new ResourceNotFoundException(reason));

            var errorResponse = ErrorResponse.builder()
                    .message(ErrorCode.RESOURCE_NOT_FOUND.getMessage())
                    .code(ErrorCode.RESOURCE_NOT_FOUND.getCode())
                    .reason(reason)
                    .build();

            // when
            var response = mockMvc.perform(post("/api/v1/dashboard/finance/confirm-bank-transfer/{appointmentId}", appointmentId))
                    .andExpect(status().isNotFound())
                    .andDo(print())
                    .andReturn().getResponse();

            // then
            verify(financeService).confirmBankTransfer(any(ConfirmBankTransferCommand.class));
            verify(financeMapper, never()).toConfirmedPaymentResponse(any());

            assertThat(response).isNotNull();
            assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(errorResponse).getJson());
        }

        @Test
        void shouldNotConfirmBankTransferOnInvalidStateTransition() throws Exception {
            // given
            var appointmentId = UUID.randomUUID();
            var reason = "Appointment is in terminal state %s, no further transitions allowed".formatted(AppointmentState.CONFIRMED.name());
            when(financeService.confirmBankTransfer(any(ConfirmBankTransferCommand.class))).thenThrow(new InvalidStateTransitionException(reason));

            var errorResponse = ErrorResponse.builder()
                    .message(ErrorCode.INVALID_STATE_TRANSITION.getMessage())
                    .code(ErrorCode.INVALID_STATE_TRANSITION.getCode())
                    .reason(reason)
                    .build();

            // when
            var response = mockMvc.perform(post("/api/v1/dashboard/finance/confirm-bank-transfer/{appointmentId}", appointmentId))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse();

            // then
            verify(financeService).confirmBankTransfer(any(ConfirmBankTransferCommand.class));
            verify(financeMapper, never()).toConfirmedPaymentResponse(any());

            assertThat(response).isNotNull();
            assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(errorResponse).getJson());
        }
    }
}