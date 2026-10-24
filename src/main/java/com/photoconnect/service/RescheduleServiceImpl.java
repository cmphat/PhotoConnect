package com.photoconnect.service;

import com.photoconnect.dto.RescheduleRequestDto;
import com.photoconnect.entity.*;
import com.photoconnect.exception.InvalidBookingException;
import com.photoconnect.exception.UnauthorizedException;
import com.photoconnect.repository.BookingHistoryRepository;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.RescheduleRequestRepository;
import com.photoconnect.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class RescheduleServiceImpl implements RescheduleService {

    private static final Logger log = LoggerFactory.getLogger(RescheduleServiceImpl.class);

    private final RescheduleRequestRepository rescheduleRequestRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ScheduleService scheduleService;
    private final BookingHistoryRepository bookingHistoryRepository;
    private final NotificationService notificationService;

    @Autowired
    public RescheduleServiceImpl(RescheduleRequestRepository rescheduleRequestRepository,
                                 BookingRepository bookingRepository,
                                 UserRepository userRepository,
                                 ScheduleService scheduleService,
                                 BookingHistoryRepository bookingHistoryRepository,
                                 @Autowired(required = false) NotificationService notificationService) {
        this.rescheduleRequestRepository = rescheduleRequestRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.scheduleService = scheduleService;
        this.bookingHistoryRepository = bookingHistoryRepository;
        this.notificationService = notificationService;
    }

    @Override
    public RescheduleRequestDto createRescheduleRequest(Long bookingId, Long actorUserId, LocalDate proposedDate, LocalTime proposedTime, String reason) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new InvalidBookingException("Booking #" + bookingId + " not found."));

        validateBookingParticipant(booking, actorUserId);

        if (booking.getStatus() != BookingStatus.ACCEPTED &&
            booking.getStatus() != BookingStatus.DEPOSIT_PENDING &&
            booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidBookingException("Rescheduling is only permitted for active sessions (Accepted, Deposit Pending, or Confirmed). Current status: " + booking.getStatus());
        }

        if (rescheduleRequestRepository.existsByBookingIdAndStatus(bookingId, RescheduleStatus.PENDING)) {
            throw new InvalidBookingException("A pending reschedule request already exists for this booking.");
        }

        if (proposedDate == null || proposedTime == null) {
            throw new InvalidBookingException("Proposed date and time are required.");
        }

        LocalDateTime proposedDateTime = LocalDateTime.of(proposedDate, proposedTime);
        if (proposedDateTime.isBefore(LocalDateTime.now().plusHours(1))) {
            throw new InvalidBookingException("Proposed session time must be at least 1 hour in the future.");
        }

        // Validate photographer slot availability
        scheduleService.validateSlotAvailability(
                booking.getPhotographerProfile().getId(),
                proposedDate,
                proposedTime,
                booking.getDurationMinutes(),
                booking.getId()
        );

        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new UnauthorizedException("User not found."));

        RescheduleRequest request = new RescheduleRequest(booking, actor, proposedDate, proposedTime, reason);
        RescheduleRequest saved = rescheduleRequestRepository.save(request);
        log.info("Created reschedule request [{}] for booking [{}] by user [{}]", saved.getId(), bookingId, actorUserId);

        // Notify other party
        if (notificationService != null) {
            Long recipientId = getOtherPartyUserId(booking, actorUserId);
            String targetUrl = isCustomer(booking, recipientId) ? "/bookings/" + bookingId : "/photographer/bookings/" + bookingId;
            String message = actor.getFullName() + " requested to reschedule Booking #" + bookingId + " to " + proposedDate + " at " + proposedTime +
                    (reason != null && !reason.isBlank() ? " (Reason: " + reason + ")" : "");

            notificationService.sendNotification(
                    recipientId,
                    NotificationType.SCHEDULE_CHANGED,
                    "Reschedule Request for Booking #" + bookingId,
                    message,
                    bookingId,
                    targetUrl
            );
        }

        return RescheduleRequestDto.from(saved);
    }

    @Override
    public void acceptReschedule(Long rescheduleRequestId, Long actorUserId, String responseNote) {
        RescheduleRequest request = rescheduleRequestRepository.findByIdWithDetails(rescheduleRequestId)
                .orElseThrow(() -> new InvalidBookingException("Reschedule request not found."));

        if (request.getStatus() != RescheduleStatus.PENDING) {
            throw new InvalidBookingException("This reschedule request is no longer pending.");
        }

        Booking booking = request.getBooking();
        validateBookingParticipant(booking, actorUserId);

        if (request.getRequestedBy().getId().equals(actorUserId)) {
            throw new UnauthorizedException("You cannot accept your own reschedule request.");
        }

        // Re-validate photographer availability
        scheduleService.validateSlotAvailability(
                booking.getPhotographerProfile().getId(),
                request.getProposedDate(),
                request.getProposedTime(),
                booking.getDurationMinutes(),
                booking.getId()
        );

        LocalDate oldDate = booking.getBookingDate();
        LocalTime oldTime = booking.getBookingTime();

        booking.setBookingDate(request.getProposedDate());
        booking.setBookingTime(request.getProposedTime());
        bookingRepository.save(booking);

        request.setStatus(RescheduleStatus.ACCEPTED);
        request.setResolvedAt(LocalDateTime.now());
        request.setResponseNote(responseNote != null && !responseNote.isBlank() ? responseNote : "Accepted");
        rescheduleRequestRepository.save(request);

        User actor = userRepository.findById(actorUserId).orElse(request.getRequestedBy());

        // Audit in booking history
        bookingHistoryRepository.save(new BookingHistory(
                booking,
                booking.getStatus(),
                booking.getStatus(),
                actor,
                "Rescheduled session from " + oldDate + " " + oldTime + " to " +
                        request.getProposedDate() + " " + request.getProposedTime() +
                        (request.getReason() != null ? " (Reason: " + request.getReason() + ")" : "")
        ));

        log.info("Accepted reschedule request [{}] for booking [{}] by user [{}]", rescheduleRequestId, booking.getId(), actorUserId);

        if (notificationService != null) {
            Long recipientId = request.getRequestedBy().getId();
            String targetUrl = isCustomer(booking, recipientId) ? "/bookings/" + booking.getId() : "/photographer/bookings/" + booking.getId();
            notificationService.sendNotification(
                    recipientId,
                    NotificationType.SCHEDULE_CHANGED,
                    "Reschedule Request Accepted",
                    actor.getFullName() + " accepted your reschedule request for Booking #" + booking.getId() + ". New session: " + request.getProposedDate() + " at " + request.getProposedTime() + ".",
                    booking.getId(),
                    targetUrl
            );
        }
    }

    @Override
    public void rejectReschedule(Long rescheduleRequestId, Long actorUserId, String reason) {
        RescheduleRequest request = rescheduleRequestRepository.findByIdWithDetails(rescheduleRequestId)
                .orElseThrow(() -> new InvalidBookingException("Reschedule request not found."));

        if (request.getStatus() != RescheduleStatus.PENDING) {
            throw new InvalidBookingException("This reschedule request is no longer pending.");
        }

        Booking booking = request.getBooking();
        validateBookingParticipant(booking, actorUserId);

        if (request.getRequestedBy().getId().equals(actorUserId)) {
            throw new UnauthorizedException("You cannot reject your own reschedule request. Use cancel instead.");
        }

        request.setStatus(RescheduleStatus.REJECTED);
        request.setResolvedAt(LocalDateTime.now());
        request.setResponseNote(reason != null && !reason.isBlank() ? reason : "Declined");
        rescheduleRequestRepository.save(request);

        User actor = userRepository.findById(actorUserId).orElse(request.getRequestedBy());
        log.info("Rejected reschedule request [{}] for booking [{}] by user [{}]", rescheduleRequestId, booking.getId(), actorUserId);

        if (notificationService != null) {
            Long recipientId = request.getRequestedBy().getId();
            String targetUrl = isCustomer(booking, recipientId) ? "/bookings/" + booking.getId() : "/photographer/bookings/" + booking.getId();
            notificationService.sendNotification(
                    recipientId,
                    NotificationType.SCHEDULE_CHANGED,
                    "Reschedule Request Declined",
                    actor.getFullName() + " declined your reschedule request for Booking #" + booking.getId() +
                            (reason != null && !reason.isBlank() ? ". Reason: " + reason : "."),
                    booking.getId(),
                    targetUrl
            );
        }
    }

    @Override
    public void cancelReschedule(Long rescheduleRequestId, Long actorUserId) {
        RescheduleRequest request = rescheduleRequestRepository.findByIdWithDetails(rescheduleRequestId)
                .orElseThrow(() -> new InvalidBookingException("Reschedule request not found."));

        if (request.getStatus() != RescheduleStatus.PENDING) {
            throw new InvalidBookingException("This reschedule request is no longer pending.");
        }

        if (!request.getRequestedBy().getId().equals(actorUserId)) {
            throw new UnauthorizedException("Only the author can cancel their own reschedule request.");
        }

        request.setStatus(RescheduleStatus.CANCELLED);
        request.setResolvedAt(LocalDateTime.now());
        request.setResponseNote("Cancelled by author");
        rescheduleRequestRepository.save(request);

        log.info("Cancelled reschedule request [{}] by author [{}]", rescheduleRequestId, actorUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RescheduleRequestDto> getRescheduleRequestsForBooking(Long bookingId, Long actorUserId) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new InvalidBookingException("Booking #" + bookingId + " not found."));

        validateBookingParticipant(booking, actorUserId);

        return rescheduleRequestRepository.findByBookingIdWithDetails(bookingId)
                .stream()
                .map(RescheduleRequestDto::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RescheduleRequestDto> getPendingRescheduleRequest(Long bookingId, Long actorUserId) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new InvalidBookingException("Booking #" + bookingId + " not found."));

        validateBookingParticipant(booking, actorUserId);

        return rescheduleRequestRepository.findFirstByBookingIdAndStatus(bookingId, RescheduleStatus.PENDING)
                .map(RescheduleRequestDto::from);
    }

    private void validateBookingParticipant(Booking booking, Long actorUserId) {
        boolean isCustomer = isCustomer(booking, actorUserId);
        boolean isPhotographer = isPhotographer(booking, actorUserId);
        if (!isCustomer && !isPhotographer) {
            throw new UnauthorizedException("You are not a participant in this booking.");
        }
    }

    private boolean isCustomer(Booking booking, Long userId) {
        return booking.getCustomer() != null && booking.getCustomer().getId().equals(userId);
    }

    private boolean isPhotographer(Booking booking, Long userId) {
        return booking.getPhotographerProfile() != null &&
                booking.getPhotographerProfile().getUser() != null &&
                booking.getPhotographerProfile().getUser().getId().equals(userId);
    }

    private Long getOtherPartyUserId(Booking booking, Long actorUserId) {
        if (isCustomer(booking, actorUserId)) {
            return booking.getPhotographerProfile().getUser().getId();
        }
        return booking.getCustomer().getId();
    }
}
