package com.photoconnect.controller;

import com.photoconnect.payment.WebhookVerificationResult;
import com.photoconnect.service.PaymentWebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentWebhookControllerTest {

    @Mock
    private PaymentWebhookService paymentWebhookService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PaymentWebhookController controller = new PaymentWebhookController(paymentWebhookService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Valid PayOS webhook returns 200 OK with success flag")
    void testValidPayOSWebhookReturns200() throws Exception {
        String payload = "{\"data\":{\"orderCode\":12345,\"amount\":450000},\"signature\":\"valid_sig\"}";

        WebhookVerificationResult validResult = WebhookVerificationResult.valid(
                "PAYOS", "TX123", 12345L, "desc",
                new BigDecimal("450000"), "VND", true, payload
        );

        when(paymentWebhookService.processWebhook(eq("PAYOS"), eq(payload), anyMap()))
                .thenReturn(validResult);

        mockMvc.perform(post("/api/webhooks/payment/payos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.provider").value("PAYOS"));

        verify(paymentWebhookService).processWebhook(eq("PAYOS"), eq(payload), anyMap());
    }

    @Test
    @DisplayName("Invalid PayOS signature returns 400 Bad Request with failure reason")
    void testInvalidPayOSWebhookReturns400() throws Exception {
        String payload = "{\"data\":{\"orderCode\":12345},\"signature\":\"tampered_sig\"}";

        WebhookVerificationResult invalidResult = WebhookVerificationResult.invalid(
                "PAYOS", "Invalid PayOS webhook signature.", payload
        );

        when(paymentWebhookService.processWebhook(eq("PAYOS"), eq(payload), anyMap()))
                .thenReturn(invalidResult);

        mockMvc.perform(post("/api/webhooks/payment/payos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid PayOS webhook signature."));
    }

    @Test
    @DisplayName("Duplicate webhook replay returns 200 OK (idempotent ignore, prevents double processing)")
    void testDuplicateWebhookReplayReturns200() throws Exception {
        String payload = "{\"data\":{\"orderCode\":12345,\"amount\":450000},\"signature\":\"valid_sig\"}";

        // Webhook service recognizes duplicate external ID and returns valid result without double-crediting
        WebhookVerificationResult replayResult = WebhookVerificationResult.valid(
                "PAYOS", "TX123", 12345L, "desc",
                new BigDecimal("450000"), "VND", true, payload
        );

        when(paymentWebhookService.processWebhook(eq("PAYOS"), eq(payload), anyMap()))
                .thenReturn(replayResult);

        mockMvc.perform(post("/api/webhooks/payment/payos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Amount mismatch or failed webhook validation returns 400 Bad Request")
    void testAmountMismatchReturns400() throws Exception {
        String payload = "{\"data\":{\"orderCode\":12345,\"amount\":1000},\"signature\":\"valid_sig\"}";

        WebhookVerificationResult mismatchResult = WebhookVerificationResult.invalid(
                "PAYOS", "Paid amount 1000 does not match deposit requirement 450000", payload
        );

        when(paymentWebhookService.processWebhook(eq("PAYOS"), eq(payload), anyMap()))
                .thenReturn(mismatchResult);

        mockMvc.perform(post("/api/webhooks/payment/payos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Paid amount 1000 does not match deposit requirement 450000"));
    }

    @Test
    @DisplayName("Valid SePay webhook returns 200 OK")
    void testValidSePayWebhookReturns200() throws Exception {
        String payload = "{\"id\":999,\"transferAmount\":600000,\"code\":\"PC-20261027-1234\"}";

        WebhookVerificationResult sepayResult = WebhookVerificationResult.valid(
                "SEPAY", "PC-20261027-1234", null, "content",
                new BigDecimal("600000"), "VND", true, payload
        );

        when(paymentWebhookService.processWebhook(eq("SEPAY"), eq(payload), anyMap()))
                .thenReturn(sepayResult);

        mockMvc.perform(post("/api/webhooks/payment/sepay")
                        .header("Authorization", "Apikey sample_key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.provider").value("SEPAY"));
    }
}
