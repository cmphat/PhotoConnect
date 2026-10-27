package com.photoconnect.payment;

import java.math.BigDecimal;

public class PaymentCreationRequest {

    private Long bookingId;
    private Long depositId;
    private Long orderCode;
    private BigDecimal amount;
    private String paymentReference;
    private String description;
    private String returnUrl;
    private String cancelUrl;
    private String customerName;
    private String customerEmail;

    public PaymentCreationRequest() {
    }

    public PaymentCreationRequest(Long bookingId, Long depositId, Long orderCode,
                                  BigDecimal amount, String paymentReference, String description,
                                  String returnUrl, String cancelUrl,
                                  String customerName, String customerEmail) {
        this.bookingId = bookingId;
        this.depositId = depositId;
        this.orderCode = orderCode;
        this.amount = amount;
        this.paymentReference = paymentReference;
        this.description = description;
        this.returnUrl = returnUrl;
        this.cancelUrl = cancelUrl;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getDepositId() {
        return depositId;
    }

    public void setDepositId(Long depositId) {
        this.depositId = depositId;
    }

    public Long getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(Long orderCode) {
        this.orderCode = orderCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getCancelUrl() {
        return cancelUrl;
    }

    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
}
