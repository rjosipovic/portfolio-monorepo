package com.studioengine.tutor.api.controller;

import com.studioengine.tutor.errors.ErrorCode;
import com.studioengine.tutor.errors.ErrorResponse;
import com.studioengine.tutor.errors.GlobalExceptionHandler;
import com.studioengine.tutor.errors.exceptions.WebhookDeserializationException;
import com.studioengine.tutor.errors.exceptions.WebhookVerificationException;
import com.studioengine.tutor.payment.PaymentProvider;
import com.studioengine.tutor.payment.PaymentService;
import com.studioengine.tutor.payment.provider.ProviderResult;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StripeWebhookControllerTest {

    @Mock
    private PaymentProvider paymentProvider;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private StripeWebhookController controller;

    private MockMvc mockMvc;

    private JacksonTester<ErrorResponse> errorResponseJson;

    @BeforeEach
    void setUp() {
        var jsonMapper = JsonMapper.builder().findAndAddModules().build();
        JacksonTester.initFields(this, jsonMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void shouldProcessValidWebhookAndReturn200() throws Exception {
        // given
        var result = ProviderResult.builder()
                .sessionId("cs_test_123")
                .paymentIntentId("pi_12345")
                .appointmentId(UUID.randomUUID())
                .amount(new BigDecimal("30.00"))
                .build();

        when(paymentProvider.processCallback(any(), eq("sig_valid"))).thenReturn(Optional.of(result));

        // when / then
        mockMvc.perform(post("/api/v1/webhooks/stripe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"checkout.session.completed\"}")
                        .header("Stripe-Signature", "sig_valid"))
                .andExpect(status().isOk());

        verify(paymentService).handleStripeWebhookConfirmation(result);
    }

    @Test
    void shouldReturn200WithoutProcessingWhenEventTypeIgnored() throws Exception {
        // given
        when(paymentProvider.processCallback(any(), eq("sig_valid"))).thenReturn(Optional.empty());

        // when / then
        mockMvc.perform(post("/api/v1/webhooks/stripe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"payment_intent.created\"}")
                        .header("Stripe-Signature", "sig_valid"))
                .andExpect(status().isOk());

        verify(paymentService, never()).handleStripeWebhookConfirmation(any());
    }

    @Test
    void shouldNotProcessWhenStripeSignatureMissing() throws Exception {
        // given / when / then
        mockMvc.perform(post("/api/v1/webhooks/stripe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"checkout.session.completed\"}"))
                .andExpect(status().isBadRequest());

        verify(paymentService, never()).handleStripeWebhookConfirmation(any());
    }

    @Test
    void shouldNotProcessWhenValidationFailed() throws Exception {
        // given
        var reason = "Stripe webhook processing failed";
        when(paymentProvider.processCallback(any(), eq("sig_valid"))).thenThrow(new WebhookVerificationException(reason));
        var expectedError = ErrorResponse.builder()
                .message(ErrorCode.WEBHOOK_VERIFICATION_FAILED.getMessage())
                .code(ErrorCode.WEBHOOK_VERIFICATION_FAILED.getCode())
                .reason(reason)
                .build();

        // when / then
        var response = mockMvc.perform(post("/api/v1/webhooks/stripe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"checkout.session.completed\"}")
                        .header("Stripe-Signature", "sig_valid"))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();

        verify(paymentService, never()).handleStripeWebhookConfirmation(any());
        assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(expectedError).getJson());
    }

    @Test
    void shouldNotProcessWhenDeserializationFailed() throws Exception {
        // given
        var reason = "Failed to deserialize Stripe checkout session for event 1234";
        when(paymentProvider.processCallback(any(), eq("sig_valid"))).thenThrow(new WebhookDeserializationException(reason));
        var expectedError = ErrorResponse.builder()
                .message(ErrorCode.WEBHOOK_DESERIALIZATION_FAILED.getMessage())
                .code(ErrorCode.WEBHOOK_DESERIALIZATION_FAILED.getCode())
                .reason(reason)
                .build();

        // when / then
        var response = mockMvc.perform(post("/api/v1/webhooks/stripe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"checkout.session.completed\"}")
                        .header("Stripe-Signature", "sig_valid"))
                .andExpect(status().isInternalServerError())
                .andReturn().getResponse();

        verify(paymentService, never()).handleStripeWebhookConfirmation(any());
        assertThat(response.getContentAsString()).isEqualTo(errorResponseJson.write(expectedError).getJson());
    }
}