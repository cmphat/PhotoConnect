package com.photoconnect.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.photoconnect.service.DemoQrCodeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentGatewayAdapterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("PayOS HMAC-SHA256 signature verification succeeds for valid sorted payload")
    void testPayOSSignatureVerification() throws Exception {
        String checksumKey = "sample_checksum_key_for_testing_123456";
        PayOSPaymentGatewayAdapter adapter = new PayOSPaymentGatewayAdapter(
                "client-123", "api-key-456", checksumKey, true, objectMapper);

        // Prepare sample PayOS webhook data payload
        String dataJson = "{\"amount\":450000,\"code\":\"00\",\"currency\":\"VND\",\"description\":\"PC2026102712345678\",\"orderCode\":1729000000000,\"reference\":\"TX998877\"}";
        var dataNode = objectMapper.readTree(dataJson);
        String calculatedSig = PayOSPaymentGatewayAdapter.generateDataSignature(dataNode, checksumKey);

        String fullWebhookPayload = String.format("{\"code\":\"00\",\"desc\":\"success\",\"data\":%s,\"signature\":\"%s\"}",
                dataJson, calculatedSig);

        WebhookVerificationResult result = adapter.verifyAndParseWebhook(fullWebhookPayload, Map.of());

        assertThat(result.isValid()).isTrue();
        assertThat(result.getProviderName()).isEqualTo("PAYOS");
        assertThat(result.getOrderCode()).isEqualTo(1729000000000L);
        assertThat(result.getAmount()).isEqualByComparingTo("450000");
        assertThat(result.isTransactionSuccessful()).isTrue();
    }

    @Test
    @DisplayName("PayOS signature verification fails if payload has been tampered with")
    void testPayOSSignatureVerificationFailsOnTamperedPayload() {
        String checksumKey = "sample_checksum_key_for_testing_123456";
        PayOSPaymentGatewayAdapter adapter = new PayOSPaymentGatewayAdapter(
                "client-123", "api-key-456", checksumKey, true, objectMapper);

        String tamperedPayload = "{\"code\":\"00\",\"desc\":\"success\",\"data\":{\"amount\":99999999,\"orderCode\":123},\"signature\":\"invalid_signature_hex\"}";

        WebhookVerificationResult result = adapter.verifyAndParseWebhook(tamperedPayload, Map.of());

        assertThat(result.isValid()).isFalse();
        assertThat(result.getFailureReason()).contains("Invalid PayOS webhook signature");
    }

    @Test
    @DisplayName("SePay verification validates Apikey header against configured secret")
    void testSePayHeaderVerification() {
        String apiKey = "sepay_secret_token_abc";
        SePayPaymentGatewayAdapter adapter = new SePayPaymentGatewayAdapter(
                apiKey, "999988887777", "MBBank", true, objectMapper);

        String sepayPayload = "{\"id\":12345,\"gateway\":\"MBBank\",\"transactionDate\":\"2026-10-27 12:00:00\",\"accountNumber\":\"999988887777\",\"code\":\"PC-20261027-12345678\",\"content\":\"Thanh toan coc PC-20261027-12345678\",\"transferType\":\"in\",\"transferAmount\":450000,\"referenceCode\":\"FT123456\"}";

        // With valid Apikey header
        WebhookVerificationResult validResult = adapter.verifyAndParseWebhook(
                sepayPayload,
                Map.of("authorization", "Apikey " + apiKey)
        );
        assertThat(validResult.isValid()).isTrue();
        assertThat(validResult.getProviderName()).isEqualTo("SEPAY");
        assertThat(validResult.getAmount()).isEqualByComparingTo("450000");
        assertThat(validResult.getPaymentReference()).isEqualTo("PC-20261027-12345678");

        // With invalid Apikey header
        WebhookVerificationResult invalidResult = adapter.verifyAndParseWebhook(
                sepayPayload,
                Map.of("authorization", "Apikey wrong_token")
        );
        assertThat(invalidResult.isValid()).isFalse();
        assertThat(invalidResult.getFailureReason()).contains("Invalid SePay authorization header");
    }

    @Test
    @DisplayName("Demo adapter creates self-contained SVG QR code with simulation label")
    void testDemoAdapterSvgGeneration() {
        DemoQrCodeService qrCodeService = new DemoQrCodeService();
        DemoPaymentGatewayAdapter demoAdapter = new DemoPaymentGatewayAdapter(qrCodeService, true);

        assertThat(demoAdapter.getType()).isEqualTo(PaymentGatewayType.DEMO);
        assertThat(demoAdapter.isAvailable()).isTrue();

        PaymentCreationRequest request = new PaymentCreationRequest(
                55L,
                1L,
                45000001L,
                new BigDecimal("600000"),
                "PC-20261027-ABCD1234",
                "Deposit #55",
                "/bookings/55/deposit/receipt",
                "/bookings/55/deposit/checkout",
                "Customer Name",
                "customer@example.com"
        );

        PaymentCreationResult result = demoAdapter.createPaymentRequest(request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getGatewayType()).isEqualTo(PaymentGatewayType.DEMO);
        assertThat(result.getQrCodeUrl()).startsWith("data:image/svg+xml;base64,");
        assertThat(result.getProviderTransactionId()).isEqualTo("DEMO-PC-20261027-ABCD1234");
    }

    @Test
    @DisplayName("Real adapters report unavailable when real mode is disabled or credentials missing")
    void testAdaptersDisabledByDefault() {
        PayOSPaymentGatewayAdapter payos = new PayOSPaymentGatewayAdapter("", "", "", false, objectMapper);
        assertThat(payos.isAvailable()).isFalse();

        SePayPaymentGatewayAdapter sepay = new SePayPaymentGatewayAdapter("", "", "MBBank", false, objectMapper);
        assertThat(sepay.isAvailable()).isFalse();
    }
}
