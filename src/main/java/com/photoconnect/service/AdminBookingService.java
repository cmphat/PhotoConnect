package com.photoconnect.service;

import com.photoconnect.dto.BookingHistoryDto;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;

import java.time.LocalDate;
import java.util.List;

public interface AdminBookingService {

    /**
     * Lists bookings for administrative monitoring, optionally filtered by status.
     * Associated customer and photographer entities are eagerly fetched.
     */
    List<Booking> listBookings(BookingStatus status);

    /**
     * Searches and filters bookings by status, keyword search across participants/location,
     * and shoot date range.
     */
    List<Booking> searchBookings(BookingStatus status, String search, LocalDate startDate, LocalDate endDate);

    /**
     * Retrieves full details for a booking including customer, photographer, and deposit.
     */
    Booking getBookingDetails(Long bookingId);

    /**
     * Retrieves the complete lifecycle audit history trail for a booking.
     */
    List<BookingHistoryDto> getBookingAuditLogs(Long bookingId);

    /**
     * Resolves an administrative cancellation dispute (e.g. REFUND_CUSTOMER or PAY_PHOTOGRAPHER).
     */
    void resolveDispute(Long bookingId, String resolution, String adminNotes, Long adminUserId);
}
