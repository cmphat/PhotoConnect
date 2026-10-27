package com.photoconnect.service;

import com.photoconnect.payment.WebhookVerificationResult;

import java.util.Map;

public interface PaymentWebhookService {

    WebhookVerificationResult processWebhook(String providerName, String rawBody, Map<String, String> headers);
}
