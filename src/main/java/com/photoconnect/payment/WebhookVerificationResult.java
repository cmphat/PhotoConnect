package com.photoconnect.payment;

import java.math.BigDecimal;

public class WebhookVerificationResult {

    private boolean valid;
    private String providerName;
    private String externalTransactionId;
    private Long orderCode;
    private String paymentReference;
    private BigDecimal amount;
    private String currency = "VND";
    private boolean transactionSuccessful;
    private String rawPayload;
    private String failureReason;

    public WebhookVerificationResult() {
    }

    public static WebhookVerificationResult valid(String providerName,
                                                 String externalTransactionId,
                                                 Long orderCode,
                                                 String paymentReference,
                                                 BigDecimal amount,
                                                 String currency,
                                                 boolean transactionSuccessful,
                                                 String rawPayload) {
        WebhookVerificationResult result = new WebhookVerificationResult();
        result.setValid(true);
        result.setProviderName(providerName);
        result.setExternalTransactionId(externalTransactionId);
        result.setOrderCode(orderCode);
        result.setPaymentReference(paymentReference);
        result.setAmount(amount);
        result.setCurrency(currency != null ? currency : "VND");
        result.setTransactionSuccessful(transactionSuccessful);
        result.setRawPayload(rawPayload);
        return result;
    }

    public static WebhookVerificationResult invalid(String providerName, String reason, String rawPayload) {
        WebhookVerificationResult result = new WebhookVerificationResult();
        result.setValid(false);
        result.setProviderName(providerName);
        result.setFailureReason(reason);
        result.setRawPayload(rawPayload);
        return result;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getExternalTransactionId() {
        return externalTransactionId;
    }

    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }

    public Long getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(Long orderCode) {
        this.orderCode = orderCode;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isTransactionSuccessful() {
        return transactionSuccessful;
    }

    public void setTransactionSuccessful(boolean transactionSuccessful) {
        this.transactionSuccessful = transactionSuccessful;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(String rawPayload) {
        this.rawPayload = rawPayload;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
