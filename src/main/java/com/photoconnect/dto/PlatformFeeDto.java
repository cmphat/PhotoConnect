package com.photoconnect.dto;

import com.photoconnect.entity.PlatformFee;
import com.photoconnect.entity.PlatformFeeStatus;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PlatformFeeDto {

    private Long id;
    private Long bookingId;
    private String photographerName;
    private Long photographerUserId;
    private BigDecimal bookingAmount;
    private BigDecimal feeRate;
    private String feeRatePercent;
    private BigDecimal feeAmount;
    private BigDecimal paidAmount;
    private BigDecimal outstandingAmount;
    private PlatformFeeStatus status;
    private String transactionCode;
    private String paymentMethod;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private String formattedCreatedAt;
    private String formattedPaidAt;

    public static PlatformFeeDto fromEntity(PlatformFee entity) {
        if (entity == null) return null;
        PlatformFeeDto dto = new PlatformFeeDto();
        dto.setId(entity.getId());
        dto.setBookingId(entity.getBooking() != null ? entity.getBooking().getId() : null);
        if (entity.getPhotographerProfile() != null) {
            dto.setPhotographerName(entity.getPhotographerProfile().getDisplayName());
        } else if (entity.getPhotographerUser() != null) {
            dto.setPhotographerName(entity.getPhotographerUser().getFullName());
        } else {
            dto.setPhotographerName("Photographer");
        }
        dto.setPhotographerUserId(entity.getPhotographerUser() != null ? entity.getPhotographerUser().getId() : null);
        dto.setBookingAmount(entity.getBookingAmount());
        dto.setFeeRate(entity.getFeeRate());
        if (entity.getFeeRate() != null) {
            dto.setFeeRatePercent((entity.getFeeRate().multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString()) + "%");
        }
        dto.setFeeAmount(entity.getFeeAmount());
        dto.setPaidAmount(entity.getPaidAmount());
        dto.setOutstandingAmount(entity.getOutstandingAmount());
        dto.setStatus(entity.getStatus());
        dto.setTransactionCode(entity.getTransactionCode());
        dto.setPaymentMethod(entity.getPaymentMethod());
        dto.setNotes(entity.getNotes());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setPaidAt(entity.getPaidAt());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        if (entity.getCreatedAt() != null) {
            dto.setFormattedCreatedAt(entity.getCreatedAt().format(dtf));
        }
        if (entity.getPaidAt() != null) {
            dto.setFormattedPaidAt(entity.getPaidAt().format(dtf));
        }
        return dto;
    }

    public String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0 VND";
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        return nf.format(amount) + " VND";
    }

    public String getFormattedBookingAmount() {
        return formatCurrency(bookingAmount);
    }

    public String getFormattedFeeAmount() {
        return formatCurrency(feeAmount);
    }

    public String getFormattedPaidAmount() {
        return formatCurrency(paidAmount);
    }

    public String getFormattedOutstandingAmount() {
        return formatCurrency(outstandingAmount);
    }

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

    public String getPhotographerName() {
        return photographerName;
    }

    public void setPhotographerName(String photographerName) {
        this.photographerName = photographerName;
    }

    public Long getPhotographerUserId() {
        return photographerUserId;
    }

    public void setPhotographerUserId(Long photographerUserId) {
        this.photographerUserId = photographerUserId;
    }

    public BigDecimal getBookingAmount() {
        return bookingAmount;
    }

    public void setBookingAmount(BigDecimal bookingAmount) {
        this.bookingAmount = bookingAmount;
    }

    public BigDecimal getFeeRate() {
        return feeRate;
    }

    public void setFeeRate(BigDecimal feeRate) {
        this.feeRate = feeRate;
    }

    public String getFeeRatePercent() {
        return feeRatePercent;
    }

    public void setFeeRatePercent(String feeRatePercent) {
        this.feeRatePercent = feeRatePercent;
    }

    public BigDecimal getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(BigDecimal feeAmount) {
        this.feeAmount = feeAmount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public BigDecimal getOutstandingAmount() {
        return outstandingAmount;
    }

    public void setOutstandingAmount(BigDecimal outstandingAmount) {
        this.outstandingAmount = outstandingAmount;
    }

    public PlatformFeeStatus getStatus() {
        return status;
    }

    public void setStatus(PlatformFeeStatus status) {
        this.status = status;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public String getFormattedCreatedAt() {
        return formattedCreatedAt;
    }

    public void setFormattedCreatedAt(String formattedCreatedAt) {
        this.formattedCreatedAt = formattedCreatedAt;
    }

    public String getFormattedPaidAt() {
        return formattedPaidAt;
    }

    public void setFormattedPaidAt(String formattedPaidAt) {
        this.formattedPaidAt = formattedPaidAt;
    }
}
