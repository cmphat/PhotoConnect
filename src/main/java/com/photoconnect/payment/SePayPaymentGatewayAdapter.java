package com.photoconnect.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Map;

@Component
public class SePayPaymentGatewayAdapter implements PaymentGatewayAdapter {

    private static final Logger log = LoggerFactory.getLogger(SePayPaymentGatewayAdapter.class);

    private final String apiKey;
    private final String accountNumber;
    private final String bankName;
    private final boolean realEnabled;
    private final ObjectMapper objectMapper;

    public SePayPaymentGatewayAdapter(
            @Value("${photoconnect.payment.sepay.api-key:}") String apiKey,
            @Value("${photoconnect.payment.sepay.account-number:}") String accountNumber,
            @Value("${photoconnect.payment.sepay.bank-name:MBBank}") String bankName,
            @Value("${photoconnect.payment.real-enabled:false}") boolean realEnabled,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.accountNumber = accountNumber != null ? accountNumber.trim() : "";
        this.bankName = bankName != null ? bankName.trim() : "MBBank";
        this.realEnabled = realEnabled;
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentGatewayType getType() {
        return PaymentGatewayType.SEPAY;
    }

    @Override
    public boolean isAvailable() {
        return realEnabled && !apiKey.isEmpty() && !accountNumber.isEmpty();
    }

    @Override
    public PaymentCreationResult createPaymentRequest(PaymentCreationRequest request) {
        if (!isAvailable()) {
            return PaymentCreationResult.failed(PaymentGatewayType.SEPAY,
                    "SePay is disabled or missing credentials (apiKey, accountNumber).");
        }

        try {
            long orderCode = request.getOrderCode() != null ? request.getOrderCode() : System.currentTimeMillis();
            String description = request.getPaymentReference();
            BigDecimal amount = request.getAmount();

            String encodedDesc = URLEncoder.encode(description, StandardCharsets.UTF_8);
            String encodedAccountName = URLEncoder.encode("PHOTOCONNECT VIETNAM", StandardCharsets.UTF_8);

            // Generate standard VietQR image link
            String qrUrl = String.format("https://img.vietqr.io/image/%s-%s-compact2.png?amount=%s&addInfo=%s&accountName=%s",
                    bankName, accountNumber, amount.toPlainString(), encodedDesc, encodedAccountName);

            PaymentCreationResult result = PaymentCreationResult.success(
                    PaymentGatewayType.SEPAY,
                    orderCode,
                    qrUrl,
                    request.getReturnUrl() != null ? request.getReturnUrl() : "/bookings/" + request.getBookingId() + "/deposit/checkout",
                    LocalDateTime.now().plusMinutes(15)
            );
            result.setProviderTransactionId("SEPAY-" + orderCode);
            return result;
        } catch (Exception e) {
            log.error("Failed to create SePay VietQR: {}", e.getMessage());
            return PaymentCreationResult.failed(PaymentGatewayType.SEPAY, "Error generating VietQR: " + e.getMessage());
        }
    }

    @Override
    public WebhookVerificationResult verifyAndParseWebhook(String rawBody, Map<String, String> headers) {
        if (apiKey.isEmpty()) {
            return WebhookVerificationResult.invalid("SEPAY", "SePay API key is not configured.", rawBody);
        }

        // Validate Authorization header: Apikey <SEPAY_API_KEY>
        String authHeader = null;
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if ("authorization".equalsIgnoreCase(entry.getKey())) {
                authHeader = entry.getValue();
                break;
            }
        }

        String expectedHeader = "Apikey " + apiKey;
        if (authHeader == null || !MessageDigest.isEqual(authHeader.getBytes(StandardCharsets.UTF_8),
                expectedHeader.getBytes(StandardCharsets.UTF_8))) {
            return WebhookVerificationResult.invalid("SEPAY", "Invalid SePay authorization header.", rawBody);
        }

        try {
            JsonNode root = objectMapper.readTree(rawBody);
            String externalId = root.path("id").asText();
            String transferType = root.path("transferType").asText("in");
            BigDecimal amount = new BigDecimal(root.path("transferAmount").asText("0"));
            String content = root.path("content").asText();
            String code = root.path("code").asText();
            String receivingAccount = root.path("accountNumber").asText();
            String paymentRef = (code != null && !code.isBlank()) ? code : content;

            if (!"in".equalsIgnoreCase(transferType)) {
                return WebhookVerificationResult.invalid("SEPAY", "Ignored non-incoming transaction.", rawBody);
            }
            if (receivingAccount.isBlank() || !MessageDigest.isEqual(
                    receivingAccount.getBytes(StandardCharsets.UTF_8),
                    accountNumber.getBytes(StandardCharsets.UTF_8))) {
                return WebhookVerificationResult.invalid("SEPAY", "Receiving account does not match configured merchant account.", rawBody);
            }

            return WebhookVerificationResult.valid(
                    "SEPAY",
                    externalId,
                    null,
                    paymentRef,
                    amount,
                    "VND",
                    true,
                    rawBody
            );
        } catch (Exception e) {
            log.error("Failed to parse SePay webhook: {}", e.getMessage());
            return WebhookVerificationResult.invalid("SEPAY", "Invalid JSON from SePay webhook.", rawBody);
        }
    }

    @Override
    public boolean cancelPaymentRequest(Long orderCode) {
        return true;
    }
}
