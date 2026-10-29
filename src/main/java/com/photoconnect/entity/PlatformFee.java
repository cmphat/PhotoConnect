package com.photoconnect.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "platform_fees")
public class PlatformFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photographer_id", nullable = false)
    private PhotographerProfile photographerProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photographer_user_id", nullable = false)
    private User photographerUser;

    @Column(name = "booking_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal bookingAmount;

    @Column(name = "fee_rate", precision = 5, scale = 4, nullable = false)
    private BigDecimal feeRate;

    @Column(name = "fee_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal feeAmount;

    @Column(name = "paid_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "outstanding_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal outstandingAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private PlatformFeeStatus status = PlatformFeeStatus.UNPAID;

    @Column(name = "transaction_code", length = 100)
    private String transactionCode;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    public PlatformFee() {
    }

    public PlatformFee(Booking booking,
                       PhotographerProfile photographerProfile,
                       User photographerUser,
                       BigDecimal bookingAmount,
                       BigDecimal feeRate,
                       BigDecimal feeAmount) {
        this.booking = booking;
        this.photographerProfile = photographerProfile;
        this.photographerUser = photographerUser;
        this.bookingAmount = bookingAmount;
        this.feeRate = feeRate;
        this.feeAmount = feeAmount;
        this.paidAmount = BigDecimal.ZERO;
        this.outstandingAmount = feeAmount;
        this.status = PlatformFeeStatus.UNPAID;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.paidAmount == null) {
            this.paidAmount = BigDecimal.ZERO;
        }
        if (this.outstandingAmount == null && this.feeAmount != null) {
            this.outstandingAmount = this.feeAmount.subtract(this.paidAmount);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public PhotographerProfile getPhotographerProfile() {
        return photographerProfile;
    }

    public void setPhotographerProfile(PhotographerProfile photographerProfile) {
        this.photographerProfile = photographerProfile;
    }

    public User getPhotographerUser() {
        return photographerUser;
    }

    public void setPhotographerUser(User photographerUser) {
        this.photographerUser = photographerUser;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}
