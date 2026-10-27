package com.photoconnect.service;

import com.photoconnect.dto.BookingHistoryDto;
import com.photoconnect.entity.*;
import com.photoconnect.exception.InvalidBookingException;
import com.photoconnect.exception.UnauthorizedException;
import com.photoconnect.repository.BookingHistoryRepository;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.DepositRepository;
import com.photoconnect.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookingStateMachineServiceImpl implements BookingStateMachineService {

    private static final Logger log = LoggerFactory.getLogger(BookingStateMachineServiceImpl.class);

    // Adjacency list of allowed state transitions
    private static final Map<BookingStatus, Set<BookingStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(BookingStatus.class);

    static {
        // From PENDING (requested)
        ALLOWED_TRANSITIONS.put(BookingStatus.PENDING, EnumSet.of(
                BookingStatus.ACCEPTED,
                BookingStatus.REJECTED,
                BookingStatus.CANCELLED,
                BookingStatus.EXPIRED
        ));

        // From ACCEPTED
        ALLOWED_TRANSITIONS.put(BookingStatus.ACCEPTED, EnumSet.of(
                BookingStatus.DEPOSIT_PENDING,
                BookingStatus.CONFIRMED,
                BookingStatus.CANCELLED,
                BookingStatus.EXPIRED,
                BookingStatus.COMPLETED // legacy direct completion path
        ));

        // From DEPOSIT_PENDING
        ALLOWED_TRANSITIONS.put(BookingStatus.DEPOSIT_PENDING, EnumSet.of(
                BookingStatus.CONFIRMED,
                BookingStatus.CANCELLED,
                BookingStatus.EXPIRED
        ));

        // From CONFIRMED
        ALLOWED_TRANSITIONS.put(BookingStatus.CONFIRMED, EnumSet.of(
                BookingStatus.SHOOTING,
                BookingStatus.POST_PROCESSING,
                BookingStatus.DELIVERED,
                BookingStatus.CANCELLED,
                BookingStatus.DISPUTED,
                BookingStatus.COMPLETED
        ));

        // From SHOOTING
        ALLOWED_TRANSITIONS.put(BookingStatus.SHOOTING, EnumSet.of(
                BookingStatus.POST_PROCESSING,
                BookingStatus.DELIVERED,
                BookingStatus.COMPLETED,
                BookingStatus.DISPUTED
        ));

        // From POST_PROCESSING
        ALLOWED_TRANSITIONS.put(BookingStatus.POST_PROCESSING, EnumSet.of(
                BookingStatus.DELIVERED,
                BookingStatus.COMPLETED,
                BookingStatus.DISPUTED
        ));

        // From DELIVERED
        ALLOWED_TRANSITIONS.put(BookingStatus.DELIVERED, EnumSet.of(
                BookingStatus.POST_PROCESSING,
                BookingStatus.COMPLETED,
                BookingStatus.DISPUTED
        ));

        // Terminal states: COMPLETED, CANCELLED, REJECTED, EXPIRED, DISPUTED
        ALLOWED_TRANSITIONS.put(BookingStatus.COMPLETED, EnumSet.of(BookingStatus.DISPUTED));
        ALLOWED_TRANSITIONS.put(BookingStatus.CANCELLED, Collections.emptySet());
        ALLOWED_TRANSITIONS.put(BookingStatus.REJECTED, Collections.emptySet());
        ALLOWED_TRANSITIONS.put(BookingStatus.EXPIRED, Collections.emptySet());
        ALLOWED_TRANSITIONS.put(BookingStatus.DISPUTED, EnumSet.of(BookingStatus.COMPLETED, BookingStatus.CANCELLED));
    }

    private final BookingRepository bookingRepository;
    private final BookingHistoryRepository bookingHistoryRepository;
    private final UserRepository userRepository;
    private final DepositRepository depositRepository;

    public BookingStateMachineServiceImpl(BookingRepository bookingRepository,
                                          BookingHistoryRepository bookingHistoryRepository,
                                          UserRepository userRepository,
                                          DepositRepository depositRepository) {
        this.bookingRepository = bookingRepository;
        this.bookingHistoryRepository = bookingHistoryRepository;
        this.userRepository = userRepository;
        this.depositRepository = depositRepository;
    }

    @Override
    public boolean isValidTransition(BookingStatus currentStatus, BookingStatus targetStatus) {
        if (currentStatus == null || targetStatus == null) {
            return false;
        }
        if (currentStatus == targetStatus) {
            return true; // Idempotent self-transition
        }
        Set<BookingStatus> allowed = ALLOWED_TRANSITIONS.get(currentStatus);
        return allowed != null && allowed.contains(targetStatus);
    }

    @Override
    @Transactional
    public Booking transition(Long bookingId, BookingStatus targetStatus, Long actorUserId, String reason) {
        if (bookingId == null || targetStatus == null || actorUserId == null) {
            throw new IllegalArgumentException("Booking ID, target status, and actor user ID are required.");
        }

        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new InvalidBookingException("Booking not found: " + bookingId));

        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new UnauthorizedException("Actor user not found: " + actorUserId));

        BookingStatus currentStatus = booking.getStatus();

        // 1. Tolerant Retry / Idempotency: same status produces no duplicate transition
        if (currentStatus == targetStatus) {
            log.info("Booking [{}] is already in status [{}]. Skipping duplicate transition.", bookingId, targetStatus);
            return booking;
        }

        // 2. Validate transition in state graph
        if (!isValidTransition(currentStatus, targetStatus)) {
            log.error("Invalid booking transition requested for [{}]: [{}] -> [{}]", bookingId, currentStatus, targetStatus);
            throw new IllegalStateException(String.format("Invalid transition: cannot move booking from %s to %s.", currentStatus, targetStatus));
        }

        // 3. Role-based authorization
        validateActorAuthorization(booking, actor, targetStatus);

        // 4. State-specific invariant validation
        validateStateInvariants(booking, targetStatus);

        // 5. Apply state change
        booking.setStatus(targetStatus);
        Booking savedBooking = bookingRepository.save(booking);

        // 6. Record audit trail
        BookingHistory history = new BookingHistory(
                savedBooking,
                currentStatus,
                targetStatus,
                actor,
                reason != null ? reason.trim() : null
        );
        bookingHistoryRepository.save(history);

        log.info("Booking [{}] transitioned successfully: [{}] -> [{}] by user [{}] ({})",
                bookingId, currentStatus, targetStatus, actor.getId(), actor.getRole());

        return savedBooking;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingHistoryDto> getHistoryForBooking(Long bookingId, Long viewerUserId) {
        if (bookingId == null) {
            return Collections.emptyList();
        }

        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new InvalidBookingException("Booking not found: " + bookingId));

        if (viewerUserId != null) {
            User viewer = userRepository.findById(viewerUserId)
                    .orElseThrow(() -> new UnauthorizedException("User not found: " + viewerUserId));

            boolean isCustomer = booking.getCustomer() != null && booking.getCustomer().getId().equals(viewerUserId);
            boolean isPhotographer = booking.getPhotographerProfile() != null &&
                                     booking.getPhotographerProfile().getUser() != null &&
                                     booking.getPhotographerProfile().getUser().getId().equals(viewerUserId);
            boolean isAdmin = viewer.getRole() == UserRole.ADMIN;

            if (!isCustomer && !isPhotographer && !isAdmin) {
                throw new UnauthorizedException("You are not authorized to view the history for this booking.");
            }
        }

        return bookingHistoryRepository.findByBookingIdWithUserOrderByChangedAtAsc(bookingId)
                .stream()
                .map(BookingHistoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    private void validateActorAuthorization(Booking booking, User actor, BookingStatus targetStatus) {
        boolean isOperationalResolver = com.photoconnect.security.RolePermissionPolicy.hasPermission(
                actor.getRole(), com.photoconnect.security.PlatformPermission.BOOKING_DISPUTE_RESOLVE);
        if (isOperationalResolver && booking.getStatus() == BookingStatus.DISPUTED
                && (targetStatus == BookingStatus.COMPLETED || targetStatus == BookingStatus.CANCELLED)) {
            return;
        }
        if (actor.getRole() == UserRole.ADMIN) {
            return; // Administrators retain broader administrative transition authority
        }

        boolean isCustomer = booking.getCustomer() != null && booking.getCustomer().getId().equals(actor.getId());
        boolean isPhotographer = booking.getPhotographerProfile() != null &&
                                 booking.getPhotographerProfile().getUser() != null &&
                                 booking.getPhotographerProfile().getUser().getId().equals(actor.getId());

        switch (targetStatus) {
            case ACCEPTED, REJECTED, SHOOTING, DELIVERED -> {
                if (!isPhotographer) {
                    throw new UnauthorizedException("Only the assigned photographer can perform this action.");
                }
            }
            case POST_PROCESSING -> {
                if (booking.getStatus() == BookingStatus.DELIVERED) {
                    if (!isCustomer && !isPhotographer) {
                        throw new UnauthorizedException("Only the booking customer or assigned photographer can return delivery to post-processing for revisions.");
                    }
                } else {
                    if (!isPhotographer) {
                        throw new UnauthorizedException("Only the assigned photographer can perform this action.");
                    }
                }
            }
            case CONFIRMED -> {
                if (!isCustomer) {
                    throw new UnauthorizedException("Only the booking customer or payment callback can confirm the booking.");
                }
            }
            case COMPLETED -> {
                if (booking.getStatus() == BookingStatus.DELIVERED) {
                    if (!isCustomer) {
                        throw new UnauthorizedException("Only the customer can accept delivery and complete the booking.");
                    }
                } else {
                    if (!isCustomer && !isPhotographer) {
                        throw new UnauthorizedException("Only booking participants can complete this booking.");
                    }
                }
            }
            case CANCELLED -> {
                if (!isCustomer && !isPhotographer) {
                    throw new UnauthorizedException("Only the booking customer or photographer can cancel this booking.");
                }
            }
            case DISPUTED -> {
                if (!isCustomer && !isPhotographer) {
                    throw new UnauthorizedException("Only booking participants can dispute this booking.");
                }
            }
            case DEPOSIT_PENDING, EXPIRED -> {
                if (!isCustomer && !isPhotographer) {
                    throw new UnauthorizedException("Only booking participants can trigger this transition.");
                }
            }
        }
    }

    private void validateStateInvariants(Booking booking, BookingStatus targetStatus) {
        // Enforce payment evidence before moving to CONFIRMED
        if (targetStatus == BookingStatus.CONFIRMED) {
            Optional<Deposit> depositOpt = depositRepository.findByBookingIdWithDetails(booking.getId());
            if (depositOpt.isEmpty() || depositOpt.get().getStatus() != DepositStatus.PAID) {
                throw new IllegalStateException("Cannot confirm booking without a confirmed PAID deposit payment.");
            }
        }
    }
}
