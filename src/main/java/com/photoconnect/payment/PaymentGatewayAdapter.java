package com.photoconnect.payment;

import java.util.Map;

public interface PaymentGatewayAdapter {

    PaymentGatewayType getType();

    boolean isAvailable();

    PaymentCreationResult createPaymentRequest(PaymentCreationRequest request);

    WebhookVerificationResult verifyAndParseWebhook(String rawBody, Map<String, String> headers);

    boolean cancelPaymentRequest(Long orderCode);
}
