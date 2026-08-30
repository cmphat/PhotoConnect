package com.photoconnect.dto;

import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Public/safe representation of a booking for UI views and status pages.
 */
public class BookingResponseDto {

    private final Long id;
    private final Long customerId;
    private final String customerName;
    private final Long photographerProfileId;
    private final String photographerDisplayName;
    private final String photographerCity;
    private final LocalDate bookingDate;
    private final LocalTime bookingTime;
    private final String location;
    private final String notes;
    private final BigDecimal agreedPrice;
    private final BookingStatus status;
    private final LocalDateTime createdAt;

    public BookingResponseDto(Long id,
                              Long customerId,
                              String customerName,
                              Long photographerProfileId,
                              String photographerDisplayName,
                              String photographerCity,
                              LocalDate bookingDate,
                              LocalTime bookingTime,
                              String location,
                              String notes,
                              BigDecimal agreedPrice,
                              BookingStatus status,
                              LocalDateTime createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.photographerProfileId = photographerProfileId;
        this.photographerDisplayName = photographerDisplayName;
        this.photographerCity = photographerCity;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.location = location;
        this.notes = notes;
        this.agreedPrice = agreedPrice;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static BookingResponseDto from(Booking booking) {
        if (booking == null) {
            return null;
        }
        return new BookingResponseDto(
                booking.getId(),
                booking.getCustomer() != null ? booking.getCustomer().getId() : null,
                booking.getCustomer() != null ? booking.getCustomer().getFullName() : null,
                booking.getPhotographerProfile() != null ? booking.getPhotographerProfile().getId() : null,
                booking.getPhotographerProfile() != null ? booking.getPhotographerProfile().getDisplayName() : null,
                booking.getPhotographerProfile() != null ? booking.getPhotographerProfile().getCity() : null,
                booking.getBookingDate(),
                booking.getBookingTime(),
                booking.getLocation(),
                booking.getNotes(),
                booking.getAgreedPrice(),
                booking.getStatus(),
                booking.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public Long getPhotographerProfileId() {
        return photographerProfileId;
    }

    public String getPhotographerDisplayName() {
        return photographerDisplayName;
    }

    public String getPhotographerCity() {
        return photographerCity;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public LocalTime getBookingTime() {
        return bookingTime;
    }

    public String getLocation() {
        return location;
    }

    public String getNotes() {
        return notes;
    }

    public BigDecimal getAgreedPrice() {
        return agreedPrice;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
