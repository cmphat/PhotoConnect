package com.photoconnect.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Database-backed time series used by the administration dashboard charts.
 */
public class AdminDashboardAnalyticsDto {

    private List<String> labels = new ArrayList<>();
    private List<Long> userRegistrations = new ArrayList<>();
    private List<Long> customerRegistrations = new ArrayList<>();
    private List<Long> photographerRegistrations = new ArrayList<>();
    private List<Long> bookings = new ArrayList<>();
    private List<BigDecimal> completedBookingValue = new ArrayList<>();
    private List<String> bookingStatusLabels = new ArrayList<>();
    private List<Long> bookingStatusCounts = new ArrayList<>();

    private List<BigDecimal> platformFeeValues = new ArrayList<>();
    private List<BigDecimal> platformFeePaidValues = new ArrayList<>();
    private List<BigDecimal> platformFeeUnpaidValues = new ArrayList<>();

    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels; }
    public List<Long> getUserRegistrations() { return userRegistrations; }
    public void setUserRegistrations(List<Long> values) { this.userRegistrations = values; }
    public List<Long> getCustomerRegistrations() { return customerRegistrations; }
    public void setCustomerRegistrations(List<Long> values) { this.customerRegistrations = values; }
    public List<Long> getPhotographerRegistrations() { return photographerRegistrations; }
    public void setPhotographerRegistrations(List<Long> values) { this.photographerRegistrations = values; }
    public List<Long> getBookings() { return bookings; }
    public void setBookings(List<Long> bookings) { this.bookings = bookings; }
    public List<BigDecimal> getCompletedBookingValue() { return completedBookingValue; }
    public void setCompletedBookingValue(List<BigDecimal> values) { this.completedBookingValue = values; }
    public List<String> getBookingStatusLabels() { return bookingStatusLabels; }
    public void setBookingStatusLabels(List<String> values) { this.bookingStatusLabels = values; }
    public List<Long> getBookingStatusCounts() { return bookingStatusCounts; }
    public void setBookingStatusCounts(List<Long> values) { this.bookingStatusCounts = values; }
    public List<BigDecimal> getPlatformFeeValues() { return platformFeeValues; }
    public void setPlatformFeeValues(List<BigDecimal> values) { this.platformFeeValues = values; }
    public List<BigDecimal> getPlatformFeePaidValues() { return platformFeePaidValues; }
    public void setPlatformFeePaidValues(List<BigDecimal> values) { this.platformFeePaidValues = values; }
    public List<BigDecimal> getPlatformFeeUnpaidValues() { return platformFeeUnpaidValues; }
    public void setPlatformFeeUnpaidValues(List<BigDecimal> values) { this.platformFeeUnpaidValues = values; }
}
