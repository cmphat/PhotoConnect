package com.photoconnect.dto;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PhotographerFeeSummaryDto {

    private BigDecimal grossEarnings = BigDecimal.ZERO;
    private BigDecimal feesIncurred = BigDecimal.ZERO;
    private BigDecimal feesPaid = BigDecimal.ZERO;
    private BigDecimal feesOutstanding = BigDecimal.ZERO;
    private BigDecimal netEarningsExpected = BigDecimal.ZERO;
    private String currentFeeRatePercent = "10%";
    private List<PlatformFeeDto> fees;

    public String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0 VND";
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        return nf.format(amount) + " VND";
    }

    public String getFormattedGrossEarnings() {
        return formatCurrency(grossEarnings);
    }

    public String getFormattedFeesIncurred() {
        return formatCurrency(feesIncurred);
    }

    public String getFormattedFeesPaid() {
        return formatCurrency(feesPaid);
    }

    public String getFormattedFeesOutstanding() {
        return formatCurrency(feesOutstanding);
    }

    public String getFormattedNetEarningsExpected() {
        return formatCurrency(netEarningsExpected);
    }

    public BigDecimal getGrossEarnings() {
        return grossEarnings;
    }

    public void setGrossEarnings(BigDecimal grossEarnings) {
        this.grossEarnings = grossEarnings;
    }

    public BigDecimal getFeesIncurred() {
        return feesIncurred;
    }

    public void setFeesIncurred(BigDecimal feesIncurred) {
        this.feesIncurred = feesIncurred;
    }

    public BigDecimal getFeesPaid() {
        return feesPaid;
    }

    public void setFeesPaid(BigDecimal feesPaid) {
        this.feesPaid = feesPaid;
    }

    public BigDecimal getFeesOutstanding() {
        return feesOutstanding;
    }

    public void setFeesOutstanding(BigDecimal feesOutstanding) {
        this.feesOutstanding = feesOutstanding;
    }

    public BigDecimal getNetEarningsExpected() {
        return netEarningsExpected;
    }

    public void setNetEarningsExpected(BigDecimal netEarningsExpected) {
        this.netEarningsExpected = netEarningsExpected;
    }

    public String getCurrentFeeRatePercent() {
        return currentFeeRatePercent;
    }

    public void setCurrentFeeRatePercent(String currentFeeRatePercent) {
        this.currentFeeRatePercent = currentFeeRatePercent;
    }

    public List<PlatformFeeDto> getFees() {
        return fees;
    }

    public void setFees(List<PlatformFeeDto> fees) {
        this.fees = fees;
    }
}
