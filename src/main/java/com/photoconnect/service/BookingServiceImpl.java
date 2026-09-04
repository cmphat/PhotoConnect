package com.photoconnect.service;

import com.photoconnect.dto.BookingRequest;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.exception.InvalidBookingException;
import com.photoconnect.exception.SelfBookingNotAllowedException;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PhotographerProfileRepository photographerProfileRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              UserRepository userRepository,
                              PhotographerProfileRepository photographerProfileRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.photographerProfileRepository = photographerProfileRepository;
    }

    @Override
    public Booking createBooking(Long customerUserId, Long photographerProfileId, BookingRequest request) {
        if (request == null) {
            throw new InvalidBookingException("Booking request cannot be null.");
        }

        if (request.getBookingDate() == null || request.getBookingDate().isBefore(LocalDate.now())) {
            throw new InvalidBookingException("Booking date cannot be in the past.");
        }

        if (request.getBookingTime() == null) {
            throw new InvalidBookingException("Booking time is required.");
        }

        if (request.getLocation() == null || request.getLocation().trim().isEmpty()) {
            throw new InvalidBookingException("Shoot location is required.");
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

        // 4. Price Snapshot
        BigDecimal agreedPrice = profile.getPriceFrom() != null ? profile.getPriceFrom() : BigDecimal.ZERO;

        // 5. Persist Booking
        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setPhotographerProfile(profile);
        booking.setBookingDate(request.getBookingDate());
        booking.setBookingTime(request.getBookingTime());
        booking.setLocation(request.getLocation().trim());
        booking.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);
        booking.setAgreedPrice(agreedPrice);
        booking.setStatus(BookingStatus.PENDING);

        return bookingRepository.save(booking);
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
        Booking booking = getBookingForCustomer(bookingId, customerUserId);

        if (booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.ACCEPTED) {
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
        } else {
            throw new InvalidBookingException("Cannot cancel booking in current state: " + booking.getStatus());
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
            throw new InvalidBookingException("You are not authorized to view this booking.");
        }

        return booking;
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<Booking> getPhotographerBookings(Long photographerUserId) {
        return bookingRepository.findByPhotographerUserIdWithDetails(photographerUserId);
    }

    @Override
    public void acceptBooking(Long bookingId, Long photographerUserId) {
        Booking booking = getBookingForPhotographer(bookingId, photographerUserId);

        // Validate photographer status
        if (booking.getPhotographerProfile().getVerificationStatus() != PhotographerVerificationStatus.APPROVED ||
            booking.getPhotographerProfile().getUser().getStatus() != UserStatus.ACTIVE) {
            throw new InvalidBookingException("Only active and approved photographers can accept bookings.");
        }

        if (booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.ACCEPTED);
            bookingRepository.save(booking);
        } else {
            throw new InvalidBookingException("Only PENDING bookings can be accepted.");
        }
    }

    @Override
    public void rejectBooking(Long bookingId, Long photographerUserId) {
        Booking booking = getBookingForPhotographer(bookingId, photographerUserId);

        if (booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.REJECTED);
            bookingRepository.save(booking);
        } else {
            throw new InvalidBookingException("Only PENDING bookings can be rejected.");
        }
    }

    @Override
    public void completeBooking(Long bookingId, Long photographerUserId) {
        Booking booking = getBookingForPhotographer(bookingId, photographerUserId);

        if (booking.getStatus() == BookingStatus.ACCEPTED) {
            booking.setStatus(BookingStatus.COMPLETED);
            bookingRepository.save(booking);
        } else {
            throw new InvalidBookingException("Only ACCEPTED bookings can be marked as completed.");
        }
    }
}
