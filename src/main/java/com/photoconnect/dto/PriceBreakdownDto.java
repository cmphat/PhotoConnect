package com.photoconnect.dto;

import java.math.BigDecimal;

/**
 * Authoritative price breakdown snapshot DTO.
 * Total = basePrice + nightSurcharge + travelSurcharge + extrasFee
 * Deposit = RoundToVnd(totalPrice * 0.30)
 * Remaining = totalPrice - depositAmount
 */
public class PriceBreakdownDto {

    private BigDecimal basePrice = BigDecimal.ZERO;
    private BigDecimal nightSurcharge = BigDecimal.ZERO;
    private boolean nightShoot;

    private int travelDistanceKm;
    private BigDecimal travelSurcharge = BigDecimal.ZERO;

    private int extraHours;
    private BigDecimal extraHoursFee = BigDecimal.ZERO;

    private int extraPhotos;
    private BigDecimal extraPhotosFee = BigDecimal.ZERO;

    private boolean additionalPhotographer;
    private BigDecimal additionalPhotographerFee = BigDecimal.ZERO;

    private BigDecimal extrasFee = BigDecimal.ZERO;
    private BigDecimal totalPrice = BigDecimal.ZERO;
    private BigDecimal depositAmount = BigDecimal.ZERO;
    private BigDecimal remainingBalance = BigDecimal.ZERO;

    public PriceBreakdownDto() {
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getNightSurcharge() {
        return nightSurcharge;
    }

    public void setNightSurcharge(BigDecimal nightSurcharge) {
        this.nightSurcharge = nightSurcharge;
    }

    public boolean isNightShoot() {
        return nightShoot;
    }

    public void setNightShoot(boolean nightShoot) {
        this.nightShoot = nightShoot;
    }

    public int getTravelDistanceKm() {
        return travelDistanceKm;
    }

    public void setTravelDistanceKm(int travelDistanceKm) {
        this.travelDistanceKm = travelDistanceKm;
    }

    public BigDecimal getTravelSurcharge() {
        return travelSurcharge;
    }

    public void setTravelSurcharge(BigDecimal travelSurcharge) {
        this.travelSurcharge = travelSurcharge;
    }

    public int getExtraHours() {
        return extraHours;
    }

    public void setExtraHours(int extraHours) {
        this.extraHours = extraHours;
    }

    public BigDecimal getExtraHoursFee() {
        return extraHoursFee;
    }

    public void setExtraHoursFee(BigDecimal extraHoursFee) {
        this.extraHoursFee = extraHoursFee;
    }

    public int getExtraPhotos() {
        return extraPhotos;
    }

    public void setExtraPhotos(int extraPhotos) {
        this.extraPhotos = extraPhotos;
    }

    public BigDecimal getExtraPhotosFee() {
        return extraPhotosFee;
    }

    public void setExtraPhotosFee(BigDecimal extraPhotosFee) {
        this.extraPhotosFee = extraPhotosFee;
    }

    public boolean isAdditionalPhotographer() {
        return additionalPhotographer;
    }

    public void setAdditionalPhotographer(boolean additionalPhotographer) {
        this.additionalPhotographer = additionalPhotographer;
    }

    public BigDecimal getAdditionalPhotographerFee() {
        return additionalPhotographerFee;
    }

    public void setAdditionalPhotographerFee(BigDecimal additionalPhotographerFee) {
        this.additionalPhotographerFee = additionalPhotographerFee;
    }

    public BigDecimal getExtrasFee() {
        return extrasFee;
    }

    public void setExtrasFee(BigDecimal extrasFee) {
        this.extrasFee = extrasFee;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(BigDecimal remainingBalance) {
        this.remainingBalance = remainingBalance;
    }
}
