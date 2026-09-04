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
     */
    Booking getBookingForCustomer(Long bookingId, Long customerUserId);

    /**
     * Retrieves all bookings for a customer.
     */
    java.util.List<Booking> getCustomerBookings(Long customerUserId);

    /**
     * Customer cancels a booking.
     */
    void cancelBooking(Long bookingId, Long customerUserId);

    /**
     * Retrieves a booking and verifies photographer ownership.
     */
    Booking getBookingForPhotographer(Long bookingId, Long photographerUserId);

    /**
     * Retrieves all bookings for a photographer.
     */
    java.util.List<Booking> getPhotographerBookings(Long photographerUserId);

    /**
     * Photographer accepts a booking.
     */
    void acceptBooking(Long bookingId, Long photographerUserId);

    /**
     * Photographer rejects a booking.
     */
    void rejectBooking(Long bookingId, Long photographerUserId);

    /**
     * Photographer completes a booking.
     */
    void completeBooking(Long bookingId, Long photographerUserId);
}
