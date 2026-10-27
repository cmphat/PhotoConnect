package com.photoconnect.payment;

import com.photoconnect.service.DemoQrCodeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class DemoPaymentGatewayAdapter implements PaymentGatewayAdapter {

    private final DemoQrCodeService qrCodeService;
    private final boolean simulationEnabled;

    public DemoPaymentGatewayAdapter(DemoQrCodeService qrCodeService,
                                     @Value("${photoconnect.payment.simulation-enabled:true}") boolean simulationEnabled) {
        this.qrCodeService = qrCodeService;
        this.simulationEnabled = simulationEnabled;
    }

    @Override
    public PaymentGatewayType getType() {
        return PaymentGatewayType.DEMO;
    }

    @Override
    public boolean isAvailable() {
        return simulationEnabled;
    }

    @Override
    public PaymentCreationResult createPaymentRequest(PaymentCreationRequest request) {
        if (!simulationEnabled) {
            return PaymentCreationResult.failed(PaymentGatewayType.DEMO, "Demo payment simulation is disabled.");
        }

        DemoQrCodeService.DemoQrCode demoQr = qrCodeService.generate(
                request.getBookingId(),
                request.getAmount(),
                request.getPaymentReference()
        );

        PaymentCreationResult result = PaymentCreationResult.success(
                PaymentGatewayType.DEMO,
                request.getOrderCode() != null ? request.getOrderCode() : System.currentTimeMillis(),
                demoQr.dataUri(),
                request.getReturnUrl() != null ? request.getReturnUrl() : "/bookings/" + request.getBookingId() + "/deposit/checkout",
                LocalDateTime.now().plusMinutes(15)
        );
        result.setProviderTransactionId("DEMO-" + request.getPaymentReference());
        return result;
    }

    @Override
    public WebhookVerificationResult verifyAndParseWebhook(String rawBody, Map<String, String> headers) {
        // Demo mode does not receive external webhooks from public banking networks
        return WebhookVerificationResult.invalid("DEMO", "Demo mode does not accept external webhooks.", rawBody);
    }

    @Override
    public boolean cancelPaymentRequest(Long orderCode) {
        return true;
    }
}
