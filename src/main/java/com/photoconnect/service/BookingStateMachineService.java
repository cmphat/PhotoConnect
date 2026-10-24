package com.photoconnect.service;

import com.photoconnect.dto.BookingHistoryDto;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;

import java.util.List;

public interface BookingStateMachineService {

    /**
     * Executes a state transition for a booking with role-based authorization,
     * transition graph validation, payment evidence checks, and history recording.
     */
    Booking transition(Long bookingId, BookingStatus targetStatus, Long actorUserId, String reason);

    /**
     * Checks if a transition from currentStatus to targetStatus is permitted.
     */
    boolean isValidTransition(BookingStatus currentStatus, BookingStatus targetStatus);

    /**
     * Retrieves the complete lifecycle audit history for a booking.
     */
    List<BookingHistoryDto> getHistoryForBooking(Long bookingId, Long viewerUserId);
}
