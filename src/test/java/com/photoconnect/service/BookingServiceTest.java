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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
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

    @Mock
    private com.photoconnect.repository.ServicePackageRepository servicePackageRepository;

    @Mock
    private com.photoconnect.repository.DepositRepository depositRepository;

    @Mock
    private com.photoconnect.repository.RescheduleRequestRepository rescheduleRequestRepository;

    @Mock
    private BookingStateMachineService bookingStateMachineService;

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
    void createBooking_withPackage_snapshotsAgreedTerms() {
        validRequest.setPackageId(55L);
        com.photoconnect.entity.ServicePackage pkg = new com.photoconnect.entity.ServicePackage();
        pkg.setId(55L);
        pkg.setPhotographerProfile(photographerProfile);
        pkg.setName("Sunset Gold Package");
        pkg.setPrice(new BigDecimal("4200000.00"));
        pkg.setDurationMinutes(180);
        pkg.setEditedPhotoCount(45);
        pkg.setDeliveryDays(5);
        pkg.setActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(photographerProfileRepository.findById(10L)).thenReturn(Optional.of(photographerProfile));
        when(scheduleService.isDateAvailable(10L, validRequest.getBookingDate())).thenReturn(true);
        when(servicePackageRepository.findByIdAndPhotographerProfileId(55L, 10L)).thenReturn(Optional.of(pkg));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking result = bookingService.createBooking(1L, 10L, validRequest);

        assertThat(result).isNotNull();
        assertThat(result.getServicePackage()).isEqualTo(pkg);
        assertThat(result.getPackageName()).isEqualTo("Sunset Gold Package");
        assertThat(result.getAgreedPrice()).isEqualByComparingTo(new BigDecimal("4200000.00"));
        assertThat(result.getDurationMinutes()).isEqualTo(180);
        assertThat(result.getEditedPhotoCount()).isEqualTo(45);
        assertThat(result.getDeliveryDays()).isEqualTo(5);
    }

    @Test
    void createBooking_withShootBrief_persistsShootBriefFields() {
        validRequest.setConceptDescription("Vintage Hanoi Autumn Street Style");
        validRequest.setMoodboardUrl("https://pinterest.com/pin/123456");
        validRequest.setBackupLocation("Indoor Studio Café, Ba Dinh");
        validRequest.setOutfitCount(3);
        validRequest.setShotList("1. Close-up portrait with autumn leaves\n2. Walking cross street wide shot");
        validRequest.setPropsNeeded("Vintage film camera, dried flower bouquet");
        validRequest.setParticipantCount(2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(photographerProfileRepository.findById(10L)).thenReturn(Optional.of(photographerProfile));
        when(scheduleService.isDateAvailable(10L, validRequest.getBookingDate())).thenReturn(true);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking result = bookingService.createBooking(1L, 10L, validRequest);

        assertThat(result).isNotNull();
        assertThat(result.getConceptDescription()).isEqualTo("Vintage Hanoi Autumn Street Style");
        assertThat(result.getMoodboardUrl()).isEqualTo("https://pinterest.com/pin/123456");
        assertThat(result.getBackupLocation()).isEqualTo("Indoor Studio Café, Ba Dinh");
        assertThat(result.getOutfitCount()).isEqualTo(3);
        assertThat(result.getShotList()).isEqualTo("1. Close-up portrait with autumn leaves\n2. Walking cross street wide shot");
        assertThat(result.getPropsNeeded()).isEqualTo("Vintage film camera, dried flower bouquet");
        assertThat(result.getParticipantCount()).isEqualTo(2);
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
        verify(bookingStateMachineService).transition(eq(50L), eq(BookingStatus.CANCELLED), eq(1L), any());
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
    void cancelBooking_withReasonAndConfirmedMoreThan48h_marksDepositRefunded() {
        Booking booking = new Booking(customer, photographerProfile, LocalDate.now().plusDays(5), LocalTime.of(15, 0), "Studio", null, new BigDecimal("2000000"));
        booking.setId(50L);
        booking.setStatus(BookingStatus.CONFIRMED);

        com.photoconnect.entity.Deposit deposit = new com.photoconnect.entity.Deposit(booking, new BigDecimal("500000"));
        deposit.setStatus(com.photoconnect.entity.DepositStatus.PAID);

        when(bookingRepository.findByIdWithDetails(50L)).thenReturn(Optional.of(booking));
        when(depositRepository.findByBookingIdWithDetails(50L)).thenReturn(Optional.of(deposit));

        bookingService.cancelBooking(50L, 1L, "Client trip cancelled");

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(booking.getCancellationReason()).isEqualTo("Client trip cancelled");
        assertThat(booking.getCancelledBy()).isEqualTo(customer);
        assertThat(booking.getCancelledAt()).isNotNull();
        assertThat(deposit.getStatus()).isEqualTo(com.photoconnect.entity.DepositStatus.REFUNDED);
        assertThat(deposit.getFailureReason()).contains("48 hours");
        verify(depositRepository).save(deposit);
    }

    @Test
    void cancelBooking_whenConfirmedLessThan48h_marksDepositForfeited() {
        Booking booking = new Booking(customer, photographerProfile, LocalDate.now().plusDays(1), LocalTime.of(10, 0), "Studio", null, new BigDecimal("2000000"));
        booking.setId(50L);
        booking.setStatus(BookingStatus.CONFIRMED);

        com.photoconnect.entity.Deposit deposit = new com.photoconnect.entity.Deposit(booking, new BigDecimal("500000"));
        deposit.setStatus(com.photoconnect.entity.DepositStatus.PAID);

        when(bookingRepository.findByIdWithDetails(50L)).thenReturn(Optional.of(booking));
        when(depositRepository.findByBookingIdWithDetails(50L)).thenReturn(Optional.of(deposit));

        bookingService.cancelBooking(50L, 1L, "Short notice cancel");

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(deposit.getStatus()).isEqualTo(com.photoconnect.entity.DepositStatus.FORFEITED);
        assertThat(deposit.getFailureReason()).contains("forfeited per cancellation policy");
        verify(depositRepository).save(deposit);
    }

    @Test
    void cancelBookingByPhotographer_whenConfirmed_marksDepositRefunded() {
        Booking booking = new Booking(customer, photographerProfile, LocalDate.now().plusDays(1), LocalTime.of(10, 0), "Studio", null, new BigDecimal("2000000"));
        booking.setId(50L);
        booking.setStatus(BookingStatus.CONFIRMED);

        com.photoconnect.entity.Deposit deposit = new com.photoconnect.entity.Deposit(booking, new BigDecimal("500000"));
        deposit.setStatus(com.photoconnect.entity.DepositStatus.PAID);

        when(bookingRepository.findByIdWithDetails(50L)).thenReturn(Optional.of(booking));
        when(depositRepository.findByBookingIdWithDetails(50L)).thenReturn(Optional.of(deposit));

        bookingService.cancelBookingByPhotographer(50L, 2L, "Equipment malfunction");

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(booking.getCancellationReason()).isEqualTo("Equipment malfunction");
        assertThat(booking.getCancelledBy()).isEqualTo(photographerUser);
        assertThat(deposit.getStatus()).isEqualTo(com.photoconnect.entity.DepositStatus.REFUNDED);
        assertThat(deposit.getFailureReason()).contains("Photographer cancelled");
        verify(depositRepository).save(deposit);
        verify(bookingStateMachineService).transition(eq(50L), eq(BookingStatus.CANCELLED), eq(2L), any());
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

    @Test
    void createBooking_slotCollision_shouldFail() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(photographerProfileRepository.findById(10L)).thenReturn(Optional.of(photographerProfile));
        when(scheduleService.isDateAvailable(10L, validRequest.getBookingDate())).thenReturn(true);
        doThrow(new PhotographerUnavailableException("The requested time slot conflicts with an existing confirmed booking."))
                .when(scheduleService).validateSlotAvailability(eq(10L), eq(validRequest.getBookingDate()), any(), anyInt(), isNull());

        assertThatThrownBy(() -> bookingService.createBooking(1L, 10L, validRequest))
                .isInstanceOf(PhotographerUnavailableException.class)
                .hasMessageContaining("conflicts with an existing confirmed booking");
    }

    @Test
    void acceptBooking_concurrencyLockAndSlotCollision_shouldFail() {
        Booking booking = new Booking(customer, photographerProfile, LocalDate.now().plusDays(2), LocalTime.of(15, 0), "Studio", null, new BigDecimal("2000000"));
        booking.setId(50L);
        booking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findByIdWithDetails(50L)).thenReturn(Optional.of(booking));
        when(photographerProfileRepository.findByIdWithLock(10L)).thenReturn(Optional.of(photographerProfile));
        doThrow(new PhotographerUnavailableException("The requested time slot conflicts with an existing confirmed booking."))
                .when(scheduleService).validateSlotAvailability(eq(10L), eq(booking.getBookingDate()), any(), anyInt(), eq(50L));

        assertThatThrownBy(() -> bookingService.acceptBooking(50L, 2L))
                .isInstanceOf(PhotographerUnavailableException.class)
                .hasMessageContaining("conflicts with an existing confirmed booking");

        verify(photographerProfileRepository).findByIdWithLock(10L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void acceptBooking_concurrencyLockAcquired_success() {
        Booking booking = new Booking(customer, photographerProfile, LocalDate.now().plusDays(2), LocalTime.of(15, 0), "Studio", null, new BigDecimal("2000000"));
        booking.setId(50L);
        booking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findByIdWithDetails(50L)).thenReturn(Optional.of(booking));
        when(photographerProfileRepository.findByIdWithLock(10L)).thenReturn(Optional.of(photographerProfile));

        bookingService.acceptBooking(50L, 2L);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.ACCEPTED);
        verify(photographerProfileRepository).findByIdWithLock(10L);
        verify(bookingRepository).save(booking);
        verify(bookingStateMachineService).transition(eq(50L), eq(BookingStatus.ACCEPTED), eq(2L), any());
    }

    @Test
    void rejectBooking_invokesStateMachineTransitionAndSetsRejected() {
        Booking booking = new Booking(customer, photographerProfile, LocalDate.now().plusDays(2), LocalTime.of(15, 0), "Studio", null, new BigDecimal("2000000"));
        booking.setId(50L);
        booking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findByIdWithDetails(50L)).thenReturn(Optional.of(booking));

        bookingService.rejectBooking(50L, 2L);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.REJECTED);
        verify(bookingStateMachineService).transition(eq(50L), eq(BookingStatus.REJECTED), eq(2L), any());
        verify(bookingRepository).save(booking);
    }

    @Test
    void completeBooking_invokesStateMachineTransitionAndSetsCompleted() {
        Booking booking = new Booking(customer, photographerProfile, LocalDate.now().plusDays(2), LocalTime.of(15, 0), "Studio", null, new BigDecimal("2000000"));
        booking.setId(50L);
        booking.setStatus(BookingStatus.ACCEPTED);

        when(bookingRepository.findByIdWithDetails(50L)).thenReturn(Optional.of(booking));

        bookingService.completeBooking(50L, 2L);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.COMPLETED);
        verify(bookingStateMachineService).transition(eq(50L), eq(BookingStatus.COMPLETED), eq(2L), any());
        verify(bookingRepository).save(booking);
    }
}
