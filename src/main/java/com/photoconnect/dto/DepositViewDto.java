package com.photoconnect.dto;

import com.photoconnect.entity.DepositStatus;
import com.photoconnect.entity.DemoPaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DepositViewDto {
    private Long id;
    private Long bookingId;
    private BigDecimal amount;
    private DepositStatus status;
    private String paymentReference;
    private DemoPaymentMethod paymentMethod;
    private String failureReason;
    private LocalDateTime paidAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public DepositStatus getStatus() {
        return status;
    }

    public void setStatus(DepositStatus status) {
        this.status = status;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public DemoPaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(DemoPaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}
