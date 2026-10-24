package com.photoconnect.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class PhotographerAnalyticsDto {

    private long totalBookings;
    private long pendingRequests;
    private long upcomingSessions;
    private long completedBookings;
    private long cancelledBookings;

    private BigDecimal grossBookingValue = BigDecimal.ZERO;
    private BigDecimal confirmedDeposits = BigDecimal.ZERO;
    private BigDecimal completedRevenue = BigDecimal.ZERO;

    private double averageRating;
    private int reviewCount;

    private List<PackageStatDto> popularPackages;
    private Map<String, Long> statusDistribution;
    private List<MonthlyTrendDto> monthlyTrends;
    private List<NotificationDto> recentNotifications;

    public PhotographerAnalyticsDto() {
    }

    public static class PackageStatDto {
        private String packageName;
        private long bookingCount;
        private BigDecimal totalRevenue;

        public PackageStatDto(String packageName, long bookingCount, BigDecimal totalRevenue) {
            this.packageName = packageName;
            this.bookingCount = bookingCount;
            this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        }

        public String getPackageName() { return packageName; }
        public long getBookingCount() { return bookingCount; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
    }

    public static class MonthlyTrendDto {
        private String monthLabel;
        private long bookingCount;
        private BigDecimal grossRevenue;
        private BigDecimal completedRevenue;

        public MonthlyTrendDto(String monthLabel, long bookingCount, BigDecimal grossRevenue, BigDecimal completedRevenue) {
            this.monthLabel = monthLabel;
            this.bookingCount = bookingCount;
            this.grossRevenue = grossRevenue != null ? grossRevenue : BigDecimal.ZERO;
            this.completedRevenue = completedRevenue != null ? completedRevenue : BigDecimal.ZERO;
        }

        public String getMonthLabel() { return monthLabel; }
        public long getBookingCount() { return bookingCount; }
        public BigDecimal getGrossRevenue() { return grossRevenue; }
        public BigDecimal getCompletedRevenue() { return completedRevenue; }
    }

    // Getters and Setters

    public long getTotalBookings() { return totalBookings; }
    public void setTotalBookings(long totalBookings) { this.totalBookings = totalBookings; }

    public long getPendingRequests() { return pendingRequests; }
    public void setPendingRequests(long pendingRequests) { this.pendingRequests = pendingRequests; }

    public long getUpcomingSessions() { return upcomingSessions; }
    public void setUpcomingSessions(long upcomingSessions) { this.upcomingSessions = upcomingSessions; }

    public long getCompletedBookings() { return completedBookings; }
    public void setCompletedBookings(long completedBookings) { this.completedBookings = completedBookings; }

    public long getCancelledBookings() { return cancelledBookings; }
    public void setCancelledBookings(long cancelledBookings) { this.cancelledBookings = cancelledBookings; }

    public BigDecimal getGrossBookingValue() { return grossBookingValue; }
    public void setGrossBookingValue(BigDecimal grossBookingValue) { this.grossBookingValue = grossBookingValue; }

    public BigDecimal getConfirmedDeposits() { return confirmedDeposits; }
    public void setConfirmedDeposits(BigDecimal confirmedDeposits) { this.confirmedDeposits = confirmedDeposits; }

    public BigDecimal getCompletedRevenue() { return completedRevenue; }
    public void setCompletedRevenue(BigDecimal completedRevenue) { this.completedRevenue = completedRevenue; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public List<PackageStatDto> getPopularPackages() { return popularPackages; }
    public void setPopularPackages(List<PackageStatDto> popularPackages) { this.popularPackages = popularPackages; }

    public Map<String, Long> getStatusDistribution() { return statusDistribution; }
    public void setStatusDistribution(Map<String, Long> statusDistribution) { this.statusDistribution = statusDistribution; }

    public List<MonthlyTrendDto> getMonthlyTrends() { return monthlyTrends; }
    public void setMonthlyTrends(List<MonthlyTrendDto> monthlyTrends) { this.monthlyTrends = monthlyTrends; }

    public List<NotificationDto> getRecentNotifications() { return recentNotifications; }
    public void setRecentNotifications(List<NotificationDto> recentNotifications) { this.recentNotifications = recentNotifications; }
}
