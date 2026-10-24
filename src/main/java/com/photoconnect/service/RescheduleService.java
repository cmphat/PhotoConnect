package com.photoconnect.service;

import com.photoconnect.dto.RescheduleRequestDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface RescheduleService {

    /**
     * Proposes a new date and time for an active booking.
     * Allowed for either the customer or photographer.
     */
    RescheduleRequestDto createRescheduleRequest(Long bookingId, Long actorUserId, LocalDate proposedDate, LocalTime proposedTime, String reason);

    /**
     * Accepts a pending reschedule request, updates the booking schedule,
     * logs the history, and notifies the requester.
     */
    void acceptReschedule(Long rescheduleRequestId, Long actorUserId, String responseNote);

    /**
     * Rejects a pending reschedule request with a reason and notifies the requester.
     */
    void rejectReschedule(Long rescheduleRequestId, Long actorUserId, String reason);

    /**
     * Cancels a pending reschedule request by its author.
     */
    void cancelReschedule(Long rescheduleRequestId, Long actorUserId);

    /**
     * Retrieves all reschedule requests for a booking, ensuring caller authorization.
     */
    List<RescheduleRequestDto> getRescheduleRequestsForBooking(Long bookingId, Long actorUserId);

    /**
     * Retrieves the current pending reschedule request for a booking, if one exists.
     */
    Optional<RescheduleRequestDto> getPendingRescheduleRequest(Long bookingId, Long actorUserId);
}
