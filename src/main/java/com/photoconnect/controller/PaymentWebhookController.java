package com.photoconnect.controller;

import com.photoconnect.payment.WebhookVerificationResult;
import com.photoconnect.service.PaymentWebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/webhooks/payment")
public class PaymentWebhookController {

    private static final Logger log = LoggerFactory.getLogger(PaymentWebhookController.class);

    private final PaymentWebhookService paymentWebhookService;

    public PaymentWebhookController(PaymentWebhookService paymentWebhookService) {
        this.paymentWebhookService = paymentWebhookService;
    }

    @PostMapping("/payos")
    public ResponseEntity<Map<String, Object>> handlePayOSWebhook(
            @RequestBody String rawPayload,
            @RequestHeader Map<String, String> headers) {
        return processCallback("PAYOS", rawPayload, headers);
    }

    @PostMapping("/sepay")
    public ResponseEntity<Map<String, Object>> handleSePayWebhook(
            @RequestBody String rawPayload,
            @RequestHeader Map<String, String> headers) {
        return processCallback("SEPAY", rawPayload, headers);
    }

    @PostMapping("/{provider}")
    public ResponseEntity<Map<String, Object>> handleGenericWebhook(
            @PathVariable("provider") String provider,
            @RequestBody String rawPayload,
            @RequestHeader Map<String, String> headers) {
        return processCallback(provider.toUpperCase(), rawPayload, headers);
    }

    private ResponseEntity<Map<String, Object>> processCallback(String provider, String rawPayload, Map<String, String> headers) {
        try {
            WebhookVerificationResult result = paymentWebhookService.processWebhook(provider, rawPayload, headers);
            if (result.isValid()) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Webhook processed successfully",
                        "provider", provider
                ));
            } else {
                log.warn("Webhook validation failed for {}: {}", provider, result.getFailureReason());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "success", false,
                        "message", result.getFailureReason() != null ? result.getFailureReason() : "Webhook validation failed"
                ));
            }
        } catch (Exception ex) {
            log.error("Internal error processing webhook for {}: {}", provider, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Internal server error"
            ));
        }
    }
}
