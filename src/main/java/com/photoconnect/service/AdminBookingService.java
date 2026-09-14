package com.photoconnect.service;

import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;

import java.util.List;

public interface AdminBookingService {

    /**
     * Lists bookings for administrative monitoring, optionally filtered by status.
     * Associated customer and photographer entities are eagerly fetched.
     */
    List<Booking> listBookings(BookingStatus status);
}
