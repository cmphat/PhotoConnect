package com.photoconnect.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Form backing and request DTO for creating a photographer booking.
 */
public class BookingRequest {

    private Long photographerId;

    @NotNull(message = "Booking date is required")
    @FutureOrPresent(message = "Booking date cannot be in the past")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate bookingDate;

    @NotNull(message = "Booking time is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime bookingTime;

    @NotBlank(message = "Shoot location is required")
    @Size(max = 255, message = "Location cannot exceed 255 characters")
    private String location;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    public BookingRequest() {
    }

    public BookingRequest(LocalDate bookingDate, LocalTime bookingTime, String location, String notes) {
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.location = location;
        this.notes = notes;
    }

    public BookingRequest(Long photographerId, LocalDate bookingDate, LocalTime bookingTime, String location, String notes) {
        this.photographerId = photographerId;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.location = location;
        this.notes = notes;
    }

    public Long getPhotographerId() {
        return photographerId;
    }

    public void setPhotographerId(Long photographerId) {
        this.photographerId = photographerId;
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
        this.location = location != null ? location.trim() : null;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes != null ? notes.trim() : null;
    }
}
