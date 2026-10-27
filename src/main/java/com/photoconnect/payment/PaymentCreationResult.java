package com.photoconnect.payment;

import java.time.LocalDateTime;

public class PaymentCreationResult {

    private boolean success;
    private PaymentGatewayType gatewayType;
    private Long providerOrderCode;
    private String providerTransactionId;
    private String qrCodeUrl;
    private String checkoutUrl;
    private LocalDateTime expiresAt;
    private String errorMessage;

    public PaymentCreationResult() {
    }

    public static PaymentCreationResult success(PaymentGatewayType type,
                                               Long orderCode,
                                               String qrCodeUrl,
                                               String checkoutUrl,
                                               LocalDateTime expiresAt) {
        PaymentCreationResult result = new PaymentCreationResult();
        result.setSuccess(true);
        result.setGatewayType(type);
        result.setProviderOrderCode(orderCode);
        result.setQrCodeUrl(qrCodeUrl);
        result.setCheckoutUrl(checkoutUrl);
        result.setExpiresAt(expiresAt);
        return result;
    }

    public static PaymentCreationResult failed(PaymentGatewayType type, String message) {
        PaymentCreationResult result = new PaymentCreationResult();
        result.setSuccess(false);
        result.setGatewayType(type);
        result.setErrorMessage(message);
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public PaymentGatewayType getGatewayType() {
        return gatewayType;
    }

    public void setGatewayType(PaymentGatewayType gatewayType) {
        this.gatewayType = gatewayType;
    }

    public Long getProviderOrderCode() {
        return providerOrderCode;
    }

    public void setProviderOrderCode(Long providerOrderCode) {
        this.providerOrderCode = providerOrderCode;
    }

    public String getProviderTransactionId() {
        return providerTransactionId;
    }

    public void setProviderTransactionId(String providerTransactionId) {
        this.providerTransactionId = providerTransactionId;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    public void setCheckoutUrl(String checkoutUrl) {
        this.checkoutUrl = checkoutUrl;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
