package com.photoconnect.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class PayOSPaymentGatewayAdapter implements PaymentGatewayAdapter {

    private static final Logger log = LoggerFactory.getLogger(PayOSPaymentGatewayAdapter.class);
    private static final String PAYOS_ENDPOINT = "https://api-merchant.payos.vn/v2/payment-requests";

    private final String clientId;
    private final String apiKey;
    private final String checksumKey;
    private final boolean realEnabled;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public PayOSPaymentGatewayAdapter(
            @Value("${photoconnect.payment.payos.client-id:}") String clientId,
            @Value("${photoconnect.payment.payos.api-key:}") String apiKey,
            @Value("${photoconnect.payment.payos.checksum-key:}") String checksumKey,
            @Value("${photoconnect.payment.real-enabled:false}") boolean realEnabled,
            ObjectMapper objectMapper) {
        this.clientId = clientId != null ? clientId.trim() : "";
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.checksumKey = checksumKey != null ? checksumKey.trim() : "";
        this.realEnabled = realEnabled;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public PaymentGatewayType getType() {
        return PaymentGatewayType.PAYOS;
    }

    @Override
    public boolean isAvailable() {
        return realEnabled && !clientId.isEmpty() && !apiKey.isEmpty() && !checksumKey.isEmpty();
    }

    @Override
    public PaymentCreationResult createPaymentRequest(PaymentCreationRequest request) {
        if (!isAvailable()) {
            return PaymentCreationResult.failed(PaymentGatewayType.PAYOS,
                    "PayOS is disabled or missing credentials (clientId, apiKey, checksumKey).");
        }

        try {
            long orderCode = request.getOrderCode() != null ? request.getOrderCode() : System.currentTimeMillis();
            int amount = request.getAmount().intValue();
            String description = sanitizeDescription(request.getPaymentReference());
            String cancelUrl = request.getCancelUrl();
            String returnUrl = request.getReturnUrl();

            // PayOS signature creation: alphabetical order
            // amount, cancelUrl, description, orderCode, returnUrl
            String signData = String.format("amount=%d&cancelUrl=%s&description=%s&orderCode=%d&returnUrl=%s",
                    amount, cancelUrl, description, orderCode, returnUrl);
            String signature = hmacSha256(checksumKey, signData);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("orderCode", orderCode);
            body.put("amount", amount);
            body.put("description", description);
            body.put("cancelUrl", cancelUrl);
            body.put("returnUrl", returnUrl);
            body.put("signature", signature);

            String requestJson = objectMapper.writeValueAsString(body);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(PAYOS_ENDPOINT))
                    .header("Content-Type", "application/json")
                    .header("x-client-id", clientId)
                    .header("x-api-key", apiKey)
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                JsonNode root = objectMapper.readTree(response.body());
                if ("00".equals(root.path("code").asText())) {
                    JsonNode data = root.path("data");
                    String qrCode = data.path("qrCode").asText();
                    String checkoutUrl = data.path("checkoutUrl").asText();
                    PaymentCreationResult res = PaymentCreationResult.success(
                            PaymentGatewayType.PAYOS,
                            orderCode,
                            qrCode,
                            checkoutUrl,
                            LocalDateTime.now().plusMinutes(15)
                    );
                    res.setProviderTransactionId(data.path("paymentLinkId").asText());
                    return res;
                } else {
                    return PaymentCreationResult.failed(PaymentGatewayType.PAYOS,
                            "PayOS error: " + root.path("desc").asText());
                }
            } else {
                return PaymentCreationResult.failed(PaymentGatewayType.PAYOS,
                        "PayOS HTTP response: " + response.statusCode());
            }
        } catch (Exception e) {
            log.error("PayOS payment request failed: {}", e.getMessage());
            return PaymentCreationResult.failed(PaymentGatewayType.PAYOS, "Exception contacting PayOS: " + e.getMessage());
        }
    }

    @Override
    public WebhookVerificationResult verifyAndParseWebhook(String rawBody, Map<String, String> headers) {
        if (checksumKey.isEmpty()) {
            return WebhookVerificationResult.invalid("PAYOS", "PayOS checksumKey is not configured.", rawBody);
        }

        try {
            JsonNode root = objectMapper.readTree(rawBody);
            String payloadSignature = root.path("signature").asText();
            JsonNode data = root.path("data");

            if (payloadSignature.isEmpty() || data.isMissingNode() || data.isNull()) {
                return WebhookVerificationResult.invalid("PAYOS", "Missing signature or data in PayOS webhook.", rawBody);
            }

            // Verify HMAC-SHA256 signature over sorted fields of data node
            String calculatedSignature = generateDataSignature(data, checksumKey);
            if (!MessageDigest.isEqual(calculatedSignature.getBytes(StandardCharsets.UTF_8),
                    payloadSignature.getBytes(StandardCharsets.UTF_8))) {
                return WebhookVerificationResult.invalid("PAYOS", "Invalid PayOS webhook signature.", rawBody);
            }

            long orderCode = data.path("orderCode").asLong();
            BigDecimal amount = new BigDecimal(data.path("amount").asText("0"));
            String reference = data.path("reference").asText();
            String description = data.path("description").asText();
            String code = data.path("code").asText();
            String currency = data.path("currency").asText("VND");

            boolean success = "00".equals(code) || "00".equals(root.path("code").asText());

            return WebhookVerificationResult.valid(
                    "PAYOS",
                    reference != null && !reference.isBlank() ? reference : String.valueOf(orderCode),
                    orderCode,
                    description,
                    amount,
                    currency,
                    success,
                    rawBody
            );
        } catch (Exception e) {
            log.error("Error verifying PayOS webhook: {}", e.getMessage());
            return WebhookVerificationResult.invalid("PAYOS", "Invalid JSON or PayOS payload format.", rawBody);
        }
    }

    @Override
    public boolean cancelPaymentRequest(Long orderCode) {
        if (!isAvailable() || orderCode == null) return false;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PAYOS_ENDPOINT + "/" + orderCode + "/cancel"))
                    .header("x-client-id", clientId)
                    .header("x-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            log.warn("Failed to cancel PayOS payment {}: {}", orderCode, e.getMessage());
            return false;
        }
    }

    public static String generateDataSignature(JsonNode data, String secretKey) {
        List<String> fieldNames = new ArrayList<>();
        Iterator<String> it = data.fieldNames();
        while (it.hasNext()) {
            fieldNames.add(it.next());
        }
        Collections.sort(fieldNames);

        StringBuilder sb = new StringBuilder();
        for (String field : fieldNames) {
            JsonNode val = data.get(field);
            String strVal = val.isNull() ? "" : val.asText();
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(field).append("=").append(strVal);
        }
        return hmacSha256(secretKey, sb.toString());
    }

    public static String hmacSha256(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hmacBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to calculate HMAC-SHA256", e);
        }
    }

    private String sanitizeDescription(String ref) {
        if (ref == null) return "PHOTOCONNECT";
        String clean = ref.replaceAll("[^a-zA-Z0-9]", "");
        return clean.length() > 25 ? clean.substring(0, 25) : clean;
    }
}
