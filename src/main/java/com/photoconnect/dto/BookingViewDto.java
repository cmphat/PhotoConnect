package com.photoconnect.dto;

import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Safe representation of a Booking for viewing in the UI.
 * Does not expose sensitive user data.
 */
public class BookingViewDto {

    private Long id;
    private String customerName;
    private String photographerName;
    private Long photographerProfileId;
    private LocalDate bookingDate;
    private LocalTime bookingTime;
    private String location;
    private String notes;
    private BigDecimal agreedPrice;
    private BookingStatus status;
    private LocalDateTime createdAt;

    public BookingViewDto() {
    }

    public static BookingViewDto from(Booking booking) {
        BookingViewDto dto = new BookingViewDto();
        dto.setId(booking.getId());
        dto.setCustomerName(booking.getCustomer().getFullName());
        dto.setPhotographerName(booking.getPhotographerProfile().getUser().getFullName());
        dto.setPhotographerProfileId(booking.getPhotographerProfile().getId());
        dto.setBookingDate(booking.getBookingDate());
        dto.setBookingTime(booking.getBookingTime());
        dto.setLocation(booking.getLocation());
        dto.setNotes(booking.getNotes());
        dto.setAgreedPrice(booking.getAgreedPrice());
        dto.setStatus(booking.getStatus());
        dto.setCreatedAt(booking.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhotographerName() {
        return photographerName;
    }

    public void setPhotographerName(String photographerName) {
        this.photographerName = photographerName;
    }

    public Long getPhotographerProfileId() {
        return photographerProfileId;
    }

    public void setPhotographerProfileId(Long photographerProfileId) {
        this.photographerProfileId = photographerProfileId;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public BigDecimal getAgreedPrice() {
        return agreedPrice;
    }

    public void setAgreedPrice(BigDecimal agreedPrice) {
        this.agreedPrice = agreedPrice;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
