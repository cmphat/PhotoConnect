package com.photoconnect.service;

import com.photoconnect.dto.BookingHistoryDto;
import com.photoconnect.entity.*;
import com.photoconnect.exception.UnauthorizedException;
import com.photoconnect.repository.BookingHistoryRepository;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.DepositRepository;
import com.photoconnect.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class AdminBookingServiceImpl implements AdminBookingService {

    private final BookingRepository bookingRepository;
    private final BookingHistoryRepository bookingHistoryRepository;
    private final DepositRepository depositRepository;
    private final BookingStateMachineService bookingStateMachineService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public AdminBookingServiceImpl(BookingRepository bookingRepository) {
        this(bookingRepository, null, null, null, null, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public AdminBookingServiceImpl(
            BookingRepository bookingRepository,
            @org.springframework.beans.factory.annotation.Autowired(required = false) BookingHistoryRepository bookingHistoryRepository,
            @org.springframework.beans.factory.annotation.Autowired(required = false) DepositRepository depositRepository,
            @org.springframework.beans.factory.annotation.Autowired(required = false) BookingStateMachineService bookingStateMachineService,
            @org.springframework.beans.factory.annotation.Autowired(required = false) UserRepository userRepository,
            @org.springframework.beans.factory.annotation.Autowired(required = false) NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.bookingHistoryRepository = bookingHistoryRepository;
        this.depositRepository = depositRepository;
        this.bookingStateMachineService = bookingStateMachineService;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> listBookings(BookingStatus status) {
        return bookingRepository.findAllWithDetails(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> searchBookings(BookingStatus status, String search, LocalDate startDate, LocalDate endDate) {
        String normalizedSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        if (normalizedSearch == null && startDate == null && endDate == null) {
            return listBookings(status);
        }
        return bookingRepository.findAdminBookings(status, normalizedSearch, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public Booking getBookingDetails(Long bookingId) {
        if (bookingId == null) {
            throw new IllegalArgumentException("Booking ID must not be null.");
        }
        return bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingHistoryDto> getBookingAuditLogs(Long bookingId) {
        if (bookingId == null || bookingHistoryRepository == null) {
            return Collections.emptyList();
        }
        return bookingHistoryRepository.findByBookingIdWithUserOrderByChangedAtAsc(bookingId).stream()
                .map(BookingHistoryDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void resolveDispute(Long bookingId, String resolution, String adminNotes, Long adminUserId) {
        if (bookingId == null) {
            throw new IllegalArgumentException("Booking ID cannot be null.");
        }
        if (adminUserId == null) {
            throw new UnauthorizedException("Admin authentication required.");
        }
        if (userRepository == null) {
            throw new IllegalStateException("User repository is required for dispute resolution.");
        }
        User adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new UnauthorizedException("Admin user not found: " + adminUserId));
        if (adminUser.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("Only administrators can resolve booking disputes.");
        }

        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        String notes = (adminNotes != null && !adminNotes.isBlank()) ? adminNotes.trim() : "Admin dispute resolution";

        if ("REFUND_CUSTOMER".equalsIgnoreCase(resolution)) {
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setCancellationReason("Admin Dispute Resolution [Refund]: " + notes);
            booking.setCancelledBy(adminUser);
            booking.setCancelledAt(LocalDateTime.now());
            bookingRepository.save(booking);

            if (bookingHistoryRepository != null) {
                BookingHistory history = new BookingHistory(
                        booking,
                        BookingStatus.DISPUTED,
                        BookingStatus.CANCELLED,
                        adminUser,
                        "Admin Dispute Resolution [Refund]: " + notes
                );
                bookingHistoryRepository.save(history);
            }

            if (depositRepository != null) {
                depositRepository.findByBookingIdWithDetails(bookingId).ifPresent(deposit -> {
                    deposit.setStatus(DepositStatus.REFUNDED);
                    deposit.setFailureReason("Dispute resolved by Admin with full customer refund: " + notes);
                    depositRepository.save(deposit);
                });
            }

            if (notificationService != null) {
                if (booking.getCustomer() != null) {
                    notificationService.sendNotification(
                            booking.getCustomer().getId(),
                            NotificationType.BOOKING_CANCELLED,
                            "Dispute Resolved — Full Refund Issued",
                            "Admin has resolved the dispute for booking #" + bookingId + " with a full refund. Note: " + notes,
                            bookingId,
                            "/bookings/" + bookingId
                    );
                }
                if (booking.getPhotographerProfile() != null && booking.getPhotographerProfile().getUser() != null) {
                    notificationService.sendNotification(
                            booking.getPhotographerProfile().getUser().getId(),
                            NotificationType.BOOKING_CANCELLED,
                            "Dispute Resolved — Booking Cancelled",
                            "Admin resolved the dispute for booking #" + bookingId + " with a refund to customer. Note: " + notes,
                            bookingId,
                            "/photographer/bookings/" + bookingId
                    );
                }
            }
        } else if ("PAY_PHOTOGRAPHER".equalsIgnoreCase(resolution)) {
            booking.setStatus(BookingStatus.COMPLETED);
            bookingRepository.save(booking);

            if (bookingHistoryRepository != null) {
                BookingHistory history = new BookingHistory(
                        booking,
                        BookingStatus.DISPUTED,
                        BookingStatus.COMPLETED,
                        adminUser,
                        "Admin Dispute Resolution [Paid]: " + notes
                );
                bookingHistoryRepository.save(history);
            }

            if (depositRepository != null) {
                depositRepository.findByBookingIdWithDetails(bookingId).ifPresent(deposit -> {
                    deposit.setStatus(DepositStatus.PAID);
                    depositRepository.save(deposit);
                });
            }

            if (notificationService != null) {
                if (booking.getCustomer() != null) {
                    notificationService.sendNotification(
                            booking.getCustomer().getId(),
                            NotificationType.DELIVERY_CONFIRMED,
                            "Dispute Closed in Favor of Photographer",
                            "Admin has reviewed dispute for booking #" + bookingId + " and marked the session completed. Note: " + notes,
                            bookingId,
                            "/bookings/" + bookingId
                    );
                }
                if (booking.getPhotographerProfile() != null && booking.getPhotographerProfile().getUser() != null) {
                    notificationService.sendNotification(
                            booking.getPhotographerProfile().getUser().getId(),
                            NotificationType.DELIVERY_CONFIRMED,
                            "Dispute Won — Session Completed",
                            "Admin resolved dispute for booking #" + bookingId + " in your favor. Note: " + notes,
                            bookingId,
                            "/photographer/bookings/" + bookingId
                    );
                }
            }
        } else {
            throw new IllegalArgumentException("Unknown dispute resolution action: " + resolution + ". Must be REFUND_CUSTOMER or PAY_PHOTOGRAPHER.");
        }
    }
}
