package com.studioengine.tutor.api.controller;

import com.studioengine.tutor.payment.PaymentProvider;
import com.studioengine.tutor.payment.PaymentService;
import com.studioengine.tutor.payment.provider.ProviderResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

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

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldProcessValidWebhookAndReturn200() throws Exception {
        // given
        var result = ProviderResult.builder()
                .sessionId("cs_test_123")
                .appointmentId(UUID.randomUUID())
                .outcome(ProviderResult.PaymentOutcome.SUCCESS)
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
}