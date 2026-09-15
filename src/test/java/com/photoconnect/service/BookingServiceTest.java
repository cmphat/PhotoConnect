package com.photoconnect.service;

import com.photoconnect.dto.BookingRequest;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.exception.InvalidBookingException;
import com.photoconnect.exception.PhotographerUnavailableException;
import com.photoconnect.exception.SelfBookingNotAllowedException;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PhotographerProfileRepository photographerProfileRepository;

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User customer;
    private User photographerUser;
    private PhotographerProfile photographerProfile;
    private BookingRequest validRequest;

    @BeforeEach
    void setUp() {
        customer = new User();
        customer.setId(1L);
        customer.setFullName("Customer Jane");
        customer.setEmail("jane@example.com");
        customer.setRole(UserRole.CUSTOMER);
        customer.setStatus(UserStatus.ACTIVE);

        photographerUser = new User();
        photographerUser.setId(2L);
        photographerUser.setFullName("Photographer Bob");
        photographerUser.setEmail("bob@example.com");
        photographerUser.setRole(UserRole.PHOTOGRAPHER);
        photographerUser.setStatus(UserStatus.ACTIVE);

        photographerProfile = new PhotographerProfile();
        photographerProfile.setId(10L);
        photographerProfile.setUser(photographerUser);
        photographerProfile.setDisplayName("Bob's Cinematic Lens");
        photographerProfile.setCity("Ho Chi Minh City");
        photographerProfile.setExperienceYears(5);
        photographerProfile.setPriceFrom(new BigDecimal("2500000.00"));
        photographerProfile.setVerificationStatus(PhotographerVerificationStatus.APPROVED);

        validRequest = new BookingRequest(
                10L,
                LocalDate.now().plusDays(5),
                LocalTime.of(14, 30),
                "Nguyen Hue Walking Street, District 1",
                "Street portrait shoot with natural light."
        );
    }

    @Test
    void createBooking_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(photographerProfileRepository.findById(10L)).thenReturn(Optional.of(photographerProfile));
        when(scheduleService.isDateAvailable(10L, validRequest.getBookingDate())).thenReturn(true);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(100L);
            return b;
        });

        Booking result = bookingService.createBooking(1L, 10L, validRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getCustomer()).isEqualTo(customer);
        assertThat(result.getPhotographerProfile()).isEqualTo(photographerProfile);
        assertThat(result.getBookingDate()).isEqualTo(validRequest.getBookingDate());
        assertThat(result.getBookingTime()).isEqualTo(validRequest.getBookingTime());
        assertThat(result.getLocation()).isEqualTo(validRequest.getLocation());
        assertThat(result.getNotes()).isEqualTo(validRequest.getNotes());
        // Price snapshot: must equal profile's current priceFrom
        assertThat(result.getAgreedPrice()).isEqualByComparingTo(new BigDecimal("2500000.00"));
        assertThat(result.getStatus()).isEqualTo(BookingStatus.PENDING);

        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_withNullPriceFrom_shouldSnapshotZero() {
        photographerProfile.setPriceFrom(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(photographerProfileRepository.findById(10L)).thenReturn(Optional.of(photographerProfile));
        when(scheduleService.isDateAvailable(10L, validRequest.getBookingDate())).thenReturn(true);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking result = bookingService.createBooking(1L, 10L, validRequest);

        assertThat(result.getAgreedPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void createBooking_selfBooking_shouldThrowSelfBookingNotAllowedException() {
        // Customer ID matches photographer user ID
        when(userRepository.findById(2L)).thenReturn(Optional.of(photographerUser));
        when(photographerProfileRepository.findById(10L)).thenReturn(Optional.of(photographerProfile));

        assertThatThrownBy(() -> bookingService.createBooking(2L, 10L, validRequest))
                .isInstanceOf(SelfBookingNotAllowedException.class)
                .hasMessageContaining("You cannot book your own photographer profile");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_photographerUnavailable_shouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(photographerProfileRepository.findById(10L)).thenReturn(Optional.of(photographerProfile));
        when(scheduleService.isDateAvailable(10L, validRequest.getBookingDate())).thenReturn(false);

        assertThatThrownBy(() -> bookingService.createBooking(1L, 10L, validRequest))
                .isInstanceOf(PhotographerUnavailableException.class)
                .hasMessageContaining("The photographer is not available on this date.");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_customerNotFound_shouldThrowInvalidBookingException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(99L, 10L, validRequest))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessageContaining("Customer account not found");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_customerInactive_shouldThrowInvalidBookingException() {
        customer.setStatus(UserStatus.INACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> bookingService.createBooking(1L, 10L, validRequest))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessageContaining("Customer account is not active");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_nonCustomerRole_shouldBeRejected() {
        customer.setRole(UserRole.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(photographerProfileRepository.findById(10L)).thenReturn(Optional.of(photographerProfile));

        assertThatThrownBy(() -> bookingService.createBooking(1L, 10L, validRequest))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessageContaining("Only customer accounts");

        verifyNoInteractions(scheduleService);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_photographerNotFound_shouldThrowInvalidBookingException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(photographerProfileRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(1L, 999L, validRequest))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessageContaining("Photographer profile not found");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_photographerPending_shouldThrowInvalidBookingException() {
        photographerProfile.setVerificationStatus(PhotographerVerificationStatus.PENDING);
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(photographerProfileRepository.findById(10L)).thenReturn(Optional.of(photographerProfile));

        assertThatThrownBy(() -> bookingService.createBooking(1L, 10L, validRequest))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessageContaining("Only approved photographers can be booked");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_photographerRejected_shouldThrowInvalidBookingException() {
        photographerProfile.setVerificationStatus(PhotographerVerificationStatus.REJECTED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(photographerProfileRepository.findById(10L)).thenReturn(Optional.of(photographerProfile));

        assertThatThrownBy(() -> bookingService.createBooking(1L, 10L, validRequest))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessageContaining("Only approved photographers can be booked");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_photographerUserInactive_shouldThrowInvalidBookingException() {
        photographerUser.setStatus(UserStatus.INACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(photographerProfileRepository.findById(10L)).thenReturn(Optional.of(photographerProfile));

        assertThatThrownBy(() -> bookingService.createBooking(1L, 10L, validRequest))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessageContaining("Photographer user account is not active");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_pastDate_shouldThrowInvalidBookingException() {
        BookingRequest pastRequest = new BookingRequest(
                10L,
                LocalDate.now().minusDays(1),
                LocalTime.of(10, 0),
                "Location",
                null
        );

        assertThatThrownBy(() -> bookingService.createBooking(1L, 10L, pastRequest))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessageContaining("Booking date cannot be in the past");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_todayWithPastTime_shouldThrowInvalidBookingException() {
        BookingRequest pastTimeRequest = new BookingRequest(
                10L,
                LocalDate.now(),
                LocalTime.now().minusMinutes(1),
                "Location",
                null
        );

        assertThatThrownBy(() -> bookingService.createBooking(1L, 10L, pastTimeRequest))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessageContaining("date and time cannot be in the past");

        verifyNoInteractions(userRepository, photographerProfileRepository, scheduleService);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_oversizedLocation_shouldRejectBeforeDatabaseAccess() {
        validRequest.setLocation("x".repeat(256));

        assertThatThrownBy(() -> bookingService.createBooking(1L, 10L, validRequest))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessageContaining("Location cannot exceed 255 characters");

        verifyNoInteractions(userRepository, photographerProfileRepository, scheduleService);
    }

    @Test
    void getBookingForCustomer_success() {
        Booking booking = new Booking(customer, photographerProfile, LocalDate.now().plusDays(2), LocalTime.of(15, 0), "Studio", null, new BigDecimal("2000000"));
        booking.setId(50L);

        when(bookingRepository.findByIdWithDetails(50L)).thenReturn(Optional.of(booking));

        Booking found = bookingService.getBookingForCustomer(50L, 1L);
        assertThat(found).isEqualTo(booking);
    }

    @Test
    void getBookingForCustomer_differentCustomer_shouldThrowInvalidBookingException() {
        Booking booking = new Booking(customer, photographerProfile, LocalDate.now().plusDays(2), LocalTime.of(15, 0), "Studio", null, new BigDecimal("2000000"));
        booking.setId(50L);

        when(bookingRepository.findByIdWithDetails(50L)).thenReturn(Optional.of(booking));

        // User 99 tries to access customer 1's booking
        assertThatThrownBy(() -> bookingService.getBookingForCustomer(50L, 99L))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessageContaining("You are not authorized to view this booking");
    }

    @Test
    void getBookingForCustomer_notFound_shouldThrowInvalidBookingException() {
        when(bookingRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingForCustomer(999L, 1L))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessageContaining("Booking not found");
    }

    // --- State Transition Tests ---

    @Test
    void cancelBooking_whenPending_shouldSucceed() {
        Booking booking = new Booking(customer, photographerProfile, LocalDate.now().plusDays(2), LocalTime.of(15, 0), "Studio", null, new BigDecimal("2000000"));
        booking.setId(50L);
        booking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findByIdWithDetails(50L)).thenReturn(Optional.of(booking));

        bookingService.cancelBooking(50L, 1L);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bookingRepository).save(booking);
    }

    @Test
    void cancelBooking_whenAccepted_shouldSucceed() {
        Booking booking = new Booking(customer, photographerProfile, LocalDate.now().plusDays(2), LocalTime.of(15, 0), "Studio", null, new BigDecimal("2000000"));
        booking.setId(50L);
        booking.setStatus(BookingStatus.ACCEPTED);

        when(bookingRepository.findByIdWithDetails(50L)).thenReturn(Optional.of(booking));

        bookingService.cancelBooking(50L, 1L);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bookingRepository).save(booking);
    }

    @Test
    void cancelBooking_whenCompleted_shouldFail() {
        Booking booking = new Booking(customer, photographerProfile, LocalDate.now().plusDays(2), LocalTime.of(15, 0), "Studio", null, new BigDecimal("2000000"));
        booking.setId(50L);
        booking.setStatus(BookingStatus.COMPLETED);

        when(bookingRepository.findByIdWithDetails(50L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(50L, 1L))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessageContaining("Cannot cancel booking");
    }

    @Test
    void acceptBooking_whenPending_shouldSucceed() {
        Booking booking = new Booking(customer, photographerProfile, LocalDate.now().plusDays(2), LocalTime.of(15, 0), "Studio", null, new BigDecimal("2000000"));
        booking.setId(50L);
        booking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findByIdWithDetails(50L)).thenReturn(Optional.of(booking));

        bookingService.acceptBooking(50L, 2L); // Photographer user ID is 2L

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.ACCEPTED);
        verify(bookingRepository).save(booking);
    }

    @Test
    void acceptBooking_whenRejected_shouldFail() {
        Booking booking = new Booking(customer, photographerProfile, LocalDate.now().plusDays(2), LocalTime.of(15, 0), "Studio", null, new BigDecimal("2000000"));
        booking.setId(50L);
        booking.setStatus(BookingStatus.REJECTED);

        when(bookingRepository.findByIdWithDetails(50L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.acceptBooking(50L, 2L))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessageContaining("Only PENDING bookings can be accepted");
    }

    @Test
    void rejectBooking_whenPending_shouldSucceed() {
        Booking booking = new Booking(customer, photographerProfile, LocalDate.now().plusDays(2), LocalTime.of(15, 0), "Studio", null, new BigDecimal("2000000"));
        booking.setId(50L);
        booking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findByIdWithDetails(50L)).thenReturn(Optional.of(booking));

        bookingService.rejectBooking(50L, 2L);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.REJECTED);
        verify(bookingRepository).save(booking);
    }

    @Test
    void completeBooking_whenAccepted_shouldSucceed() {
        Booking booking = new Booking(customer, photographerProfile, LocalDate.now().plusDays(2), LocalTime.of(15, 0), "Studio", null, new BigDecimal("2000000"));
        booking.setId(50L);
        booking.setStatus(BookingStatus.ACCEPTED);

        when(bookingRepository.findByIdWithDetails(50L)).thenReturn(Optional.of(booking));

        bookingService.completeBooking(50L, 2L);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.COMPLETED);
        verify(bookingRepository).save(booking);
    }

    @Test
    void completeBooking_whenPending_shouldFail() {
        Booking booking = new Booking(customer, photographerProfile, LocalDate.now().plusDays(2), LocalTime.of(15, 0), "Studio", null, new BigDecimal("2000000"));
        booking.setId(50L);
        booking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findByIdWithDetails(50L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.completeBooking(50L, 2L))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessageContaining("Only ACCEPTED bookings can be marked as completed");
    }

    @Test
    void getBookingForPhotographer_wrongPhotographer_shouldFail() {
        Booking booking = new Booking(customer, photographerProfile, LocalDate.now().plusDays(2), LocalTime.of(15, 0), "Studio", null, new BigDecimal("2000000"));
        booking.setId(50L);

        when(bookingRepository.findByIdWithDetails(50L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.getBookingForPhotographer(50L, 99L))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessageContaining("You are not authorized to view this booking");
    }
}
