package com.photoconnect.service;

import com.photoconnect.dto.BookingRequest;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.exception.ErrorCode;
import com.photoconnect.exception.InvalidBookingException;
import com.photoconnect.exception.PhotographerUnavailableException;
import com.photoconnect.exception.SelfBookingNotAllowedException;
import com.photoconnect.entity.*;
import com.photoconnect.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PhotographerProfileRepository photographerProfileRepository;
    private final ScheduleService scheduleService;
    private final com.photoconnect.repository.ServicePackageRepository servicePackageRepository;
    private final NotificationService notificationService;
    private final BookingStateMachineService bookingStateMachineService;
    private final DepositRepository depositRepository;
    private final RescheduleRequestRepository rescheduleRequestRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              UserRepository userRepository,
                              PhotographerProfileRepository photographerProfileRepository,
                              ScheduleService scheduleService) {
        this(bookingRepository, userRepository, photographerProfileRepository, scheduleService, null, null, null, null, null);
    }

    public BookingServiceImpl(BookingRepository bookingRepository,
                              UserRepository userRepository,
                              PhotographerProfileRepository photographerProfileRepository,
                              ScheduleService scheduleService,
                              com.photoconnect.repository.ServicePackageRepository servicePackageRepository) {
        this(bookingRepository, userRepository, photographerProfileRepository, scheduleService, servicePackageRepository, null, null, null, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public BookingServiceImpl(BookingRepository bookingRepository,
                              UserRepository userRepository,
                              PhotographerProfileRepository photographerProfileRepository,
                              ScheduleService scheduleService,
                              @org.springframework.beans.factory.annotation.Autowired(required = false) com.photoconnect.repository.ServicePackageRepository servicePackageRepository,
                              @org.springframework.beans.factory.annotation.Autowired(required = false) NotificationService notificationService,
                              @org.springframework.beans.factory.annotation.Autowired(required = false) BookingStateMachineService bookingStateMachineService,
                              @org.springframework.beans.factory.annotation.Autowired(required = false) DepositRepository depositRepository,
                              @org.springframework.beans.factory.annotation.Autowired(required = false) RescheduleRequestRepository rescheduleRequestRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.photographerProfileRepository = photographerProfileRepository;
        this.scheduleService = scheduleService;
        this.servicePackageRepository = servicePackageRepository;
        this.notificationService = notificationService;
        this.bookingStateMachineService = bookingStateMachineService;
        this.depositRepository = depositRepository;
        this.rescheduleRequestRepository = rescheduleRequestRepository;
    }

    @Override
    public Booking createBooking(Long customerUserId, Long photographerProfileId, BookingRequest request) {
        if (customerUserId == null || photographerProfileId == null) {
            throw new InvalidBookingException("Customer and photographer are required.");
        }
        if (request == null) {
            throw new InvalidBookingException("Booking request cannot be null.");
        }

        if (request.getBookingDate() == null || request.getBookingDate().isBefore(LocalDate.now())) {
            throw new InvalidBookingException("Booking date cannot be in the past.");
        }

        if (request.getBookingTime() == null) {
            throw new InvalidBookingException("Booking time is required.");
        }

        if (LocalDateTime.of(request.getBookingDate(), request.getBookingTime()).isBefore(LocalDateTime.now())) {
            throw new InvalidBookingException("Booking date and time cannot be in the past.");
        }

        if (request.getLocation() == null || request.getLocation().trim().isEmpty()) {
            throw new InvalidBookingException("Shoot location is required.");
        }

        if (request.getLocation().trim().length() > 255) {
            throw new InvalidBookingException("Location cannot exceed 255 characters.");
        }

        if (request.getNotes() != null && request.getNotes().trim().length() > 1000) {
            throw new InvalidBookingException("Notes cannot exceed 1000 characters.");
        }

        // 1. Validate Customer
        User customer = userRepository.findById(customerUserId)
                .orElseThrow(() -> new InvalidBookingException("Customer account not found."));

        if (customer.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidBookingException("Customer account is not active.");
        }

        // 2. Validate Photographer
        PhotographerProfile profile = photographerProfileRepository.findById(photographerProfileId)
                .orElseThrow(() -> new InvalidBookingException("Photographer profile not found."));

        if (profile.getVerificationStatus() != PhotographerVerificationStatus.APPROVED) {
            throw new InvalidBookingException("Only approved photographers can be booked.");
        }

        if (profile.getUser() == null || profile.getUser().getStatus() != UserStatus.ACTIVE) {
            throw new InvalidBookingException("Photographer user account is not active.");
        }

        // 3. Prevent Self-Booking
        if (customer.getId().equals(profile.getUser().getId())) {
            throw new SelfBookingNotAllowedException("You cannot book your own photographer profile.");
        }

        if (customer.getRole() != UserRole.CUSTOMER) {
            throw new InvalidBookingException("Only customer accounts can create bookings.");
        }

        // 4. Price & Package Snapshot
        BigDecimal agreedPrice = profile.getPriceFrom() != null ? profile.getPriceFrom() : BigDecimal.ZERO;
        com.photoconnect.entity.ServicePackage selectedPackage = null;
        String packageName = "Custom Session";
        int durationMinutes = 120;
        Integer editedPhotoCount = null;
        Integer deliveryDays = null;

        if (request.getPackageId() != null && servicePackageRepository != null) {
            com.photoconnect.entity.ServicePackage pkg = servicePackageRepository
                    .findByIdAndPhotographerProfileId(request.getPackageId(), profile.getId())
                    .orElseThrow(() -> new InvalidBookingException("Selected service package not found or does not belong to this photographer."));
            if (!pkg.isActive()) {
                throw new InvalidBookingException("Selected service package is currently inactive.");
            }
            selectedPackage = pkg;
            packageName = pkg.getName();
            agreedPrice = pkg.getPrice();
            durationMinutes = pkg.getDurationMinutes();
            editedPhotoCount = pkg.getEditedPhotoCount();
            deliveryDays = pkg.getDeliveryDays();
        }

        // 5. Check Availability & Slot Conflict
        java.time.LocalTime requestedTime = request.getBookingTime() != null ? request.getBookingTime() : java.time.LocalTime.of(9, 0);
        if (!scheduleService.isDateAvailable(profile.getId(), request.getBookingDate())) {
            throw new PhotographerUnavailableException("The photographer is not available on this date.");
        }
        scheduleService.validateSlotAvailability(
                profile.getId(),
                request.getBookingDate(),
                requestedTime,
                durationMinutes,
                null
        );

        // 6. Persist Booking
        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setPhotographerProfile(profile);
        booking.setServicePackage(selectedPackage);
        booking.setPackageName(packageName);
        booking.setDurationMinutes(durationMinutes);
        booking.setEditedPhotoCount(editedPhotoCount);
        booking.setDeliveryDays(deliveryDays);
        booking.setBookingDate(request.getBookingDate());
        booking.setBookingTime(request.getBookingTime());
        booking.setLocation(request.getLocation().trim());
        booking.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);
        booking.setConceptDescription(request.getConceptDescription() != null ? request.getConceptDescription().trim() : null);
        booking.setMoodboardUrl(request.getMoodboardUrl() != null ? request.getMoodboardUrl().trim() : null);
        booking.setBackupLocation(request.getBackupLocation() != null ? request.getBackupLocation().trim() : null);
        booking.setOutfitCount(request.getOutfitCount());
        booking.setShotList(request.getShotList() != null ? request.getShotList().trim() : null);
        booking.setPropsNeeded(request.getPropsNeeded() != null ? request.getPropsNeeded().trim() : null);
        booking.setParticipantCount(request.getParticipantCount());
        booking.setAgreedPrice(agreedPrice);
        booking.setStatus(BookingStatus.PENDING);

        Booking saved = bookingRepository.save(booking);
        if (notificationService != null && profile.getUser() != null) {
            notificationService.sendNotification(
                    profile.getUser().getId(),
                    com.photoconnect.entity.NotificationType.BOOKING_REQUEST,
                    "New Booking Request",
                    "Client " + customer.getFullName() + " requested a photoshoot session for " + booking.getBookingDate(),
                    saved.getId(),
                    "/photographer/bookings/" + saved.getId()
            );
        }
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Booking getBookingForCustomer(Long bookingId, Long customerUserId) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new InvalidBookingException("Booking not found."));

        if (booking.getCustomer() == null || !booking.getCustomer().getId().equals(customerUserId)) {
            throw new InvalidBookingException("You are not authorized to view this booking.");
        }

        return booking;
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<Booking> getCustomerBookings(Long customerUserId) {
        return bookingRepository.findByCustomerIdWithDetails(customerUserId);
    }

    @Override
    public void cancelBooking(Long bookingId, Long customerUserId) {
        cancelBooking(bookingId, customerUserId, null);
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId, Long customerUserId, String reason) {
        Booking booking = getBookingForCustomer(bookingId, customerUserId);

        if (booking.getStatus() != BookingStatus.PENDING &&
            booking.getStatus() != BookingStatus.ACCEPTED &&
            booking.getStatus() != BookingStatus.DEPOSIT_PENDING &&
            booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidBookingException("Cannot cancel booking in current state: " + booking.getStatus());
        }

        String effectiveReason = (reason != null && !reason.isBlank()) ? reason : "Cancelled by client";
        booking.setCancellationReason(effectiveReason);
        booking.setCancelledBy(booking.getCustomer());
        booking.setCancelledAt(LocalDateTime.now());

        handleDepositCancellation(booking, true);
        cancelPendingReschedules(bookingId);

        bookingRepository.save(booking);

        if (bookingStateMachineService != null) {
            bookingStateMachineService.transition(bookingId, BookingStatus.CANCELLED, customerUserId, effectiveReason);
        }
        booking.setStatus(BookingStatus.CANCELLED);

        if (notificationService != null && booking.getPhotographerProfile() != null && booking.getPhotographerProfile().getUser() != null) {
            notificationService.sendNotification(
                    booking.getPhotographerProfile().getUser().getId(),
                    com.photoconnect.entity.NotificationType.BOOKING_CANCELLED,
                    "Booking Cancelled",
                    "Booking #" + bookingId + " was cancelled by the client." + (reason != null && !reason.isBlank() ? " Reason: " + reason : ""),
                    bookingId,
                    "/photographer/bookings/" + bookingId
            );
        }
    }

    @Override
    @Transactional
    public void cancelBookingByPhotographer(Long bookingId, Long photographerUserId, String reason) {
        Booking booking = getBookingForPhotographer(bookingId, photographerUserId);

        if (booking.getStatus() != BookingStatus.PENDING &&
            booking.getStatus() != BookingStatus.ACCEPTED &&
            booking.getStatus() != BookingStatus.DEPOSIT_PENDING &&
            booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidBookingException(ErrorCode.BOOKING_003_INVALID_STATUS, "Cannot cancel booking in current state: " + booking.getStatus());
        }

        String effectiveReason = (reason != null && !reason.isBlank()) ? reason : "Cancelled by photographer";
        booking.setCancellationReason(effectiveReason);
        booking.setCancelledBy(booking.getPhotographerProfile().getUser());
        booking.setCancelledAt(LocalDateTime.now());

        handleDepositCancellation(booking, false);
        cancelPendingReschedules(bookingId);

        bookingRepository.save(booking);

        if (bookingStateMachineService != null) {
            bookingStateMachineService.transition(bookingId, BookingStatus.CANCELLED, photographerUserId, effectiveReason);
        }
        booking.setStatus(BookingStatus.CANCELLED);

        if (notificationService != null && booking.getCustomer() != null) {
            notificationService.sendNotification(
                    booking.getCustomer().getId(),
                    com.photoconnect.entity.NotificationType.BOOKING_CANCELLED,
                    "Booking Cancelled",
                    "Booking #" + bookingId + " was cancelled by the photographer." + (reason != null && !reason.isBlank() ? " Reason: " + reason : "") + " Any deposit paid is eligible for 100% refund credit.",
                    bookingId,
                    "/bookings/" + bookingId
            );
        }
    }

    private void handleDepositCancellation(Booking booking, boolean cancelledByCustomer) {
        if (depositRepository == null) return;
        depositRepository.findByBookingIdWithDetails(booking.getId()).ifPresent(deposit -> {
            if (deposit.getStatus() == DepositStatus.PAID) {
                if (!cancelledByCustomer) {
                    deposit.setStatus(DepositStatus.REFUNDED);
                    deposit.setFailureReason("Photographer cancelled confirmed booking. Customer entitled to 100% refund credit.");
                } else {
                    LocalDateTime sessionStart = LocalDateTime.of(booking.getBookingDate(), booking.getBookingTime());
                    long hoursUntilSession = java.time.Duration.between(LocalDateTime.now(), sessionStart).toHours();
                    if (hoursUntilSession >= 48) {
                        deposit.setStatus(DepositStatus.REFUNDED);
                        deposit.setFailureReason("Customer cancelled >= 48 hours before session. Eligible for full refund credit (policy: 48h advance notice).");
                    } else {
                        deposit.setStatus(DepositStatus.FORFEITED);
                        deposit.setFailureReason("Customer cancelled within 48 hours of session. Deposit forfeited per cancellation policy.");
                    }
                }
                depositRepository.save(deposit);
            } else if (deposit.getStatus() == DepositStatus.PENDING || deposit.getStatus() == DepositStatus.PROCESSING) {
                deposit.setStatus(DepositStatus.CANCELLED);
                deposit.setFailureReason("Booking cancelled.");
                depositRepository.save(deposit);
            }
        });
    }

    private void cancelPendingReschedules(Long bookingId) {
        if (rescheduleRequestRepository == null) return;
        List<RescheduleRequest> pending = rescheduleRequestRepository.findByBookingIdAndStatus(bookingId, RescheduleStatus.PENDING);
        for (RescheduleRequest req : pending) {
            req.setStatus(RescheduleStatus.CANCELLED);
            req.setResolvedAt(LocalDateTime.now());
            req.setResponseNote("Auto-cancelled due to booking cancellation");
            rescheduleRequestRepository.save(req);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Booking getBookingForPhotographer(Long bookingId, Long photographerUserId) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new InvalidBookingException("Booking not found."));

        if (booking.getPhotographerProfile() == null || 
            booking.getPhotographerProfile().getUser() == null || 
            !booking.getPhotographerProfile().getUser().getId().equals(photographerUserId)) {
            throw new InvalidBookingException(ErrorCode.BOOKING_005_NOT_OWNER, "You are not authorized to view this booking.");
        }

        return booking;
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<Booking> getPhotographerBookings(Long photographerUserId) {
        return bookingRepository.findByPhotographerUserIdWithDetails(photographerUserId);
    }

    @Override
    @Transactional
    public void acceptBooking(Long bookingId, Long photographerUserId) {
        Booking booking = getBookingForPhotographer(bookingId, photographerUserId);

        // Validate photographer status
        if (booking.getPhotographerProfile().getVerificationStatus() != PhotographerVerificationStatus.APPROVED ||
            booking.getPhotographerProfile().getUser().getStatus() != UserStatus.ACTIVE) {
            throw new InvalidBookingException("Only active and approved photographers can accept bookings.");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new InvalidBookingException(ErrorCode.BOOKING_003_INVALID_STATUS, "Only PENDING bookings can be accepted.");
        }

        // Concurrency control: acquire database lock on photographer profile to prevent race conditions
        PhotographerProfile lockedProfile = photographerProfileRepository.findByIdWithLock(booking.getPhotographerProfile().getId())
                .orElse(booking.getPhotographerProfile());

        int duration = (booking.getDurationMinutes() != null && booking.getDurationMinutes() > 0)
                ? booking.getDurationMinutes()
                : 120;
        java.time.LocalTime time = booking.getBookingTime() != null ? booking.getBookingTime() : java.time.LocalTime.of(9, 0);

        // Check that slot has not been reserved concurrently
        scheduleService.validateSlotAvailability(
                lockedProfile.getId(),
                booking.getBookingDate(),
                time,
                duration,
                booking.getId()
        );

        if (bookingStateMachineService != null) {
            bookingStateMachineService.transition(booking.getId(), BookingStatus.ACCEPTED, photographerUserId, "Photographer accepted booking request");
        }
        booking.setStatus(BookingStatus.ACCEPTED);
        bookingRepository.save(booking);

        if (notificationService != null && booking.getCustomer() != null) {
            String photogName = lockedProfile.getUser() != null ? lockedProfile.getUser().getFullName() : "Your photographer";
            notificationService.sendNotification(
                    booking.getCustomer().getId(),
                    com.photoconnect.entity.NotificationType.BOOKING_ACCEPTED,
                    "Booking Accepted",
                    "Photographer " + photogName + " accepted your booking for " + booking.getBookingDate() + "!",
                    booking.getId(),
                    "/bookings/" + booking.getId()
            );
        }
    }

    @Override
    public void rejectBooking(Long bookingId, Long photographerUserId) {
        Booking booking = getBookingForPhotographer(bookingId, photographerUserId);

        if (booking.getStatus() == BookingStatus.PENDING) {
            if (bookingStateMachineService != null) {
                bookingStateMachineService.transition(bookingId, BookingStatus.REJECTED, photographerUserId, "Photographer declined booking request");
            }
            booking.setStatus(BookingStatus.REJECTED);
            bookingRepository.save(booking);

            if (notificationService != null && booking.getCustomer() != null) {
                notificationService.sendNotification(
                        booking.getCustomer().getId(),
                        com.photoconnect.entity.NotificationType.BOOKING_REJECTED,
                        "Booking Request Declined",
                        "Your booking request for " + booking.getBookingDate() + " was declined.",
                        booking.getId(),
                        "/bookings/" + booking.getId()
                );
            }
        } else {
            throw new InvalidBookingException(ErrorCode.BOOKING_003_INVALID_STATUS, "Only PENDING bookings can be rejected.");
        }
    }

    @Override
    public void completeBooking(Long bookingId, Long photographerUserId) {
        Booking booking = getBookingForPhotographer(bookingId, photographerUserId);

        if (booking.getStatus() == BookingStatus.ACCEPTED) {
            if (bookingStateMachineService != null) {
                bookingStateMachineService.transition(bookingId, BookingStatus.COMPLETED, photographerUserId, "Booking completed by photographer");
            }
            booking.setStatus(BookingStatus.COMPLETED);
            bookingRepository.save(booking);
        } else {
            throw new InvalidBookingException(ErrorCode.BOOKING_003_INVALID_STATUS, "Only ACCEPTED bookings can be marked as completed.");
        }
    }

    @Override
    @Transactional
    public void updatePhotographerNotes(Long bookingId, Long photographerUserId, String notes) {
        Booking booking = getBookingForPhotographer(bookingId, photographerUserId);
        if (notes != null && notes.trim().length() > 1500) {
            throw new InvalidBookingException("Ghi chú nội bộ của photographer không được vượt quá 1500 ký tự.");
        }
        booking.setPhotographerNotes(notes != null && !notes.isBlank() ? notes.trim() : null);
        bookingRepository.save(booking);
    }

    @Override
    public long getCompletedShootCountWithCustomer(Long photographerProfileId, Long customerId) {
        if (photographerProfileId == null || customerId == null) {
            return 0L;
        }
        return bookingRepository.countCompletedShootsForCustomerAndPhotographer(photographerProfileId, customerId);
    }
}
