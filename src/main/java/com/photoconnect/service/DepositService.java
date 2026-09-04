package com.photoconnect.service;

import com.photoconnect.dto.DepositViewDto;

public interface DepositService {

    /**
     * Creates a new PENDING deposit for an ACCEPTED booking if one doesn't exist,
     * or returns the existing deposit.
     * 
     * @param bookingId the ID of the booking
     * @param customerUserId the ID of the authenticated customer
     * @return DepositViewDto representing the deposit
     */
    DepositViewDto getOrCreateDepositForBooking(Long bookingId, Long customerUserId);

    /**
     * Retrieves the deposit for a customer's booking.
     * 
     * @param bookingId the ID of the booking
     * @param customerUserId the ID of the authenticated customer
     * @return DepositViewDto representing the deposit, or null if it doesn't exist
     */
    DepositViewDto getCustomerDeposit(Long bookingId, Long customerUserId);

    /**
     * Simulates a successful payment for a PENDING deposit in development environment.
     * 
     * @param bookingId the ID of the booking
     * @param customerUserId the ID of the authenticated customer
     * @return DepositViewDto representing the PAID deposit
     */
    DepositViewDto simulateSuccessfulPayment(Long bookingId, Long customerUserId);
}
