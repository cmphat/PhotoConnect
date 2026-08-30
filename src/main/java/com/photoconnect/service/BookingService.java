package com.photoconnect.service;

import com.photoconnect.dto.BookingRequest;
import com.photoconnect.entity.Booking;

/**
 * Service handling customer photographer booking creation and customer retrieval.
 */
public interface BookingService {

    /**
     * Creates a new booking request for an approved photographer.
     *
     * @param customerUserId        the authenticated user ID of the customer
     * @param photographerProfileId the target photographer profile ID
     * @param request               the booking details (date, time, location, notes)
     * @return the persisted Booking entity
     */
    Booking createBooking(Long customerUserId, Long photographerProfileId, BookingRequest request);

    /**
     * Retrieves a booking and verifies customer ownership.
     *
     * @param bookingId      the booking ID
     * @param customerUserId the authenticated customer user ID
     * @return the Booking entity
     */
    Booking getBookingForCustomer(Long bookingId, Long customerUserId);
}
