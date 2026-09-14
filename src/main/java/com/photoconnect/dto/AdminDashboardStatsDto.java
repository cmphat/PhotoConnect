package com.photoconnect.dto;

import java.math.BigDecimal;

/**
 * Encapsulates high-level platform statistics and KPIs for the administrative dashboard.
 */
public class AdminDashboardStatsDto {

    private long totalUsers;
    private long totalCustomers;
    private long totalPhotographers;
    private long pendingPhotographerApprovals;
    private long approvedPhotographers;
    private long totalBookings;
    private long pendingBookings;
    private long completedBookings;
    private long totalReviews;
    private long paidDepositsCount;
    private BigDecimal simulatedDepositAmount;

    public AdminDashboardStatsDto() {
        this.simulatedDepositAmount = BigDecimal.ZERO;
    }

    public AdminDashboardStatsDto(long totalUsers,
                                 long totalCustomers,
                                 long totalPhotographers,
                                 long pendingPhotographerApprovals,
                                 long approvedPhotographers,
                                 long totalBookings,
                                 long pendingBookings,
                                 long completedBookings,
                                 long totalReviews,
                                 long paidDepositsCount,
                                 BigDecimal simulatedDepositAmount) {
        this.totalUsers = totalUsers;
        this.totalCustomers = totalCustomers;
        this.totalPhotographers = totalPhotographers;
        this.pendingPhotographerApprovals = pendingPhotographerApprovals;
        this.approvedPhotographers = approvedPhotographers;
        this.totalBookings = totalBookings;
        this.pendingBookings = pendingBookings;
        this.completedBookings = completedBookings;
        this.totalReviews = totalReviews;
        this.paidDepositsCount = paidDepositsCount;
        this.simulatedDepositAmount = simulatedDepositAmount != null ? simulatedDepositAmount : BigDecimal.ZERO;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public long getTotalPhotographers() {
        return totalPhotographers;
    }

    public void setTotalPhotographers(long totalPhotographers) {
        this.totalPhotographers = totalPhotographers;
    }

    public long getPendingPhotographerApprovals() {
        return pendingPhotographerApprovals;
    }

    public void setPendingPhotographerApprovals(long pendingPhotographerApprovals) {
        this.pendingPhotographerApprovals = pendingPhotographerApprovals;
    }

    public long getApprovedPhotographers() {
        return approvedPhotographers;
    }

    public void setApprovedPhotographers(long approvedPhotographers) {
        this.approvedPhotographers = approvedPhotographers;
    }

    public long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(long totalBookings) {
        this.totalBookings = totalBookings;
    }

    public long getPendingBookings() {
        return pendingBookings;
    }

    public void setPendingBookings(long pendingBookings) {
        this.pendingBookings = pendingBookings;
    }

    public long getCompletedBookings() {
        return completedBookings;
    }

    public void setCompletedBookings(long completedBookings) {
        this.completedBookings = completedBookings;
    }

    public long getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(long totalReviews) {
        this.totalReviews = totalReviews;
    }

    public long getPaidDepositsCount() {
        return paidDepositsCount;
    }

    public void setPaidDepositsCount(long paidDepositsCount) {
        this.paidDepositsCount = paidDepositsCount;
    }

    public BigDecimal getSimulatedDepositAmount() {
        return simulatedDepositAmount;
    }

    public void setSimulatedDepositAmount(BigDecimal simulatedDepositAmount) {
        this.simulatedDepositAmount = simulatedDepositAmount;
    }
}
