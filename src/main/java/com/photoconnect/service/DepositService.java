package com.photoconnect.service;

import com.photoconnect.dto.DemoPaymentRequest;
import com.photoconnect.dto.DepositViewDto;

import java.math.BigDecimal;

public interface DepositService {

    /**
     * Calculates the required deposit using the same server-side rule as checkout.
     * This is side-effect free so read-only views can show the amount without
     * creating a payment record.
     */
    BigDecimal calculateDepositAmount(BigDecimal agreedPrice);

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

    /** Processes one deterministic, local-only demo checkout attempt. */
    DepositViewDto processDemoPayment(Long bookingId, Long customerUserId, DemoPaymentRequest request);

    /** Cancels an unpaid demo attempt without changing the associated booking. */
    DepositViewDto cancelDemoPayment(Long bookingId, Long customerUserId);

    /**
     * Simulates a successful payment for a PENDING deposit in development environment.
     * 
     * @param bookingId the ID of the booking
     * @param customerUserId the ID of the authenticated customer
     * @return DepositViewDto representing the PAID deposit
     */
    @Deprecated(forRemoval = false)
    DepositViewDto simulateSuccessfulPayment(Long bookingId, Long customerUserId);
}
