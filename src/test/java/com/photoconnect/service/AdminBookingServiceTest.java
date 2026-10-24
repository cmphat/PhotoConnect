package com.photoconnect.service;

import com.photoconnect.dto.BookingHistoryDto;
import com.photoconnect.entity.*;
import com.photoconnect.exception.ErrorCode;
import com.photoconnect.exception.InvalidBookingException;
import com.photoconnect.exception.UnauthorizedException;
import com.photoconnect.repository.BookingHistoryRepository;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.DepositRepository;
import com.photoconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminBookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private BookingHistoryRepository bookingHistoryRepository;
    @Mock private DepositRepository depositRepository;
    @Mock private BookingStateMachineService bookingStateMachineService;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;

    private AdminBookingService adminBookingService;

    @BeforeEach
    void setUp() {
        adminBookingService = new AdminBookingServiceImpl(
                bookingRepository,
                bookingHistoryRepository,
                depositRepository,
                bookingStateMachineService,
                userRepository,
                notificationService
        );
    }

    @Test
    void listBookings_nullStatus_shouldCallFindAllWithDetailsNull() {
        Booking b = new Booking();
        b.setId(1L);
        when(bookingRepository.findAllWithDetails(null)).thenReturn(List.of(b));

        List<Booking> result = adminBookingService.listBookings(null);

        assertEquals(1, result.size());
        verify(bookingRepository).findAllWithDetails(null);
    }

    @Test
    void listBookings_withStatus_shouldFilterByStatus() {
        Booking b = new Booking();
        b.setId(2L);
        b.setStatus(BookingStatus.COMPLETED);
        when(bookingRepository.findAllWithDetails(BookingStatus.COMPLETED)).thenReturn(List.of(b));

        List<Booking> result = adminBookingService.listBookings(BookingStatus.COMPLETED);

        assertEquals(1, result.size());
        assertEquals(BookingStatus.COMPLETED, result.get(0).getStatus());
        verify(bookingRepository).findAllWithDetails(BookingStatus.COMPLETED);
    }

    @Test
    void searchBookings_withFilters_shouldCallFindAdminBookings() {
        LocalDate start = LocalDate.of(2026, 10, 1);
        LocalDate end = LocalDate.of(2026, 10, 31);
        Booking b = new Booking();
        b.setId(5L);
        when(bookingRepository.findAdminBookings(BookingStatus.DISPUTED, "Da Nang", start, end))
                .thenReturn(List.of(b));

        List<Booking> result = adminBookingService.searchBookings(BookingStatus.DISPUTED, "Da Nang", start, end);

        assertEquals(1, result.size());
        verify(bookingRepository).findAdminBookings(BookingStatus.DISPUTED, "Da Nang", start, end);
    }

    @Test
    void getBookingDetails_found_returnsBooking() {
        Booking b = new Booking();
        b.setId(10L);
        when(bookingRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(b));

        Booking result = adminBookingService.getBookingDetails(10L);

        assertEquals(10L, result.getId());
    }

    @Test
    void resolveDispute_refundCustomer_transitionsAndRefundsDeposit() {
        User admin = new User();
        admin.setId(99L);
        admin.setRole(UserRole.ADMIN);
        when(userRepository.findById(99L)).thenReturn(Optional.of(admin));

        User customer = new User();
        customer.setId(10L);
        PhotographerProfile profile = new PhotographerProfile();
        User photographer = new User();
        photographer.setId(20L);
        profile.setUser(photographer);

        Booking booking = new Booking();
        booking.setId(100L);
        booking.setStatus(BookingStatus.DISPUTED);
        booking.setCustomer(customer);
        booking.setPhotographerProfile(profile);

        when(bookingRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(booking));

        Deposit deposit = new Deposit(booking, new BigDecimal("1000000"));
        deposit.setStatus(DepositStatus.PAID);
        when(depositRepository.findByBookingIdWithDetails(100L)).thenReturn(Optional.of(deposit));

        adminBookingService.resolveDispute(100L, "REFUND_CUSTOMER", "Photographer failed to arrive.", 99L);

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals(DepositStatus.REFUNDED, deposit.getStatus());
        verify(bookingRepository).save(booking);
        verify(depositRepository).save(deposit);
        verify(bookingHistoryRepository).save(any(BookingHistory.class));
        verify(notificationService).sendNotification(eq(10L), eq(NotificationType.BOOKING_CANCELLED), anyString(), anyString(), eq(100L), anyString());
        verify(notificationService).sendNotification(eq(20L), eq(NotificationType.BOOKING_CANCELLED), anyString(), anyString(), eq(100L), anyString());
    }

    @Test
    void resolveDispute_payPhotographer_transitionsAndPays() {
        User admin = new User();
        admin.setId(99L);
        admin.setRole(UserRole.ADMIN);
        when(userRepository.findById(99L)).thenReturn(Optional.of(admin));

        User customer = new User();
        customer.setId(10L);
        PhotographerProfile profile = new PhotographerProfile();
        User photographer = new User();
        photographer.setId(20L);
        profile.setUser(photographer);

        Booking booking = new Booking();
        booking.setId(100L);
        booking.setStatus(BookingStatus.DISPUTED);
        booking.setCustomer(customer);
        booking.setPhotographerProfile(profile);

        when(bookingRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(booking));

        Deposit deposit = new Deposit(booking, new BigDecimal("1000000"));
        deposit.setStatus(DepositStatus.PAID);
        when(depositRepository.findByBookingIdWithDetails(100L)).thenReturn(Optional.of(deposit));

        adminBookingService.resolveDispute(100L, "PAY_PHOTOGRAPHER", "Shoot was delivered in accordance with package.", 99L);

        assertEquals(BookingStatus.COMPLETED, booking.getStatus());
        assertEquals(DepositStatus.PAID, deposit.getStatus());
        verify(bookingRepository).save(booking);
        verify(depositRepository).save(deposit);
        verify(bookingHistoryRepository).save(any(BookingHistory.class));
        verify(notificationService).sendNotification(eq(10L), eq(NotificationType.DELIVERY_CONFIRMED), anyString(), anyString(), eq(100L), anyString());
        verify(notificationService).sendNotification(eq(20L), eq(NotificationType.DELIVERY_CONFIRMED), anyString(), anyString(), eq(100L), anyString());
    }

    @Test
    void resolveDispute_nonAdmin_throwsUnauthorized() {
        User customer = new User();
        customer.setId(10L);
        customer.setRole(UserRole.CUSTOMER);
        when(userRepository.findById(10L)).thenReturn(Optional.of(customer));

        assertThrows(UnauthorizedException.class, () ->
                adminBookingService.resolveDispute(100L, "REFUND_CUSTOMER", "notes", 10L));
    }

    @Test
    void resolveDispute_whenBookingInConfirmedStatus_throwsInvalidBookingExceptionWithConflictErrorCode() {
        User admin = new User();
        admin.setId(99L);
        admin.setRole(UserRole.ADMIN);
        when(userRepository.findById(99L)).thenReturn(Optional.of(admin));

        Booking booking = new Booking();
        booking.setId(101L);
        booking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findByIdWithDetails(101L)).thenReturn(Optional.of(booking));

        InvalidBookingException ex = assertThrows(InvalidBookingException.class, () ->
                adminBookingService.resolveDispute(101L, "REFUND_CUSTOMER", "Customer request", 99L));

        assertEquals(ErrorCode.BOOKING_003_INVALID_STATUS, ex.getErrorCode());
        verify(bookingRepository, never()).save(any());
        verify(depositRepository, never()).save(any());
    }

    @Test
    void resolveDispute_whenBookingInPendingStatus_throwsInvalidBookingExceptionWithConflictErrorCode() {
        User admin = new User();
        admin.setId(99L);
        admin.setRole(UserRole.ADMIN);
        when(userRepository.findById(99L)).thenReturn(Optional.of(admin));

        Booking booking = new Booking();
        booking.setId(102L);
        booking.setStatus(BookingStatus.PENDING);
        when(bookingRepository.findByIdWithDetails(102L)).thenReturn(Optional.of(booking));

        InvalidBookingException ex = assertThrows(InvalidBookingException.class, () ->
                adminBookingService.resolveDispute(102L, "REFUND_CUSTOMER", "Customer request", 99L));

        assertEquals(ErrorCode.BOOKING_003_INVALID_STATUS, ex.getErrorCode());
        verify(bookingRepository, never()).save(any());
        verify(depositRepository, never()).save(any());
    }

    @Test
    void resolveDispute_whenBookingInCompletedStatus_throwsInvalidBookingExceptionWithConflictErrorCode() {
        User admin = new User();
        admin.setId(99L);
        admin.setRole(UserRole.ADMIN);
        when(userRepository.findById(99L)).thenReturn(Optional.of(admin));

        Booking booking = new Booking();
        booking.setId(103L);
        booking.setStatus(BookingStatus.COMPLETED);
        when(bookingRepository.findByIdWithDetails(103L)).thenReturn(Optional.of(booking));

        InvalidBookingException ex = assertThrows(InvalidBookingException.class, () ->
                adminBookingService.resolveDispute(103L, "PAY_PHOTOGRAPHER", "Admin review", 99L));

        assertEquals(ErrorCode.BOOKING_003_INVALID_STATUS, ex.getErrorCode());
        verify(bookingRepository, never()).save(any());
        verify(depositRepository, never()).save(any());
    }

    @Test
    void resolveDispute_whenBookingInCancelledStatus_throwsInvalidBookingExceptionWithConflictErrorCode() {
        User admin = new User();
        admin.setId(99L);
        admin.setRole(UserRole.ADMIN);
        when(userRepository.findById(99L)).thenReturn(Optional.of(admin));

        Booking booking = new Booking();
        booking.setId(104L);
        booking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findByIdWithDetails(104L)).thenReturn(Optional.of(booking));

        InvalidBookingException ex = assertThrows(InvalidBookingException.class, () ->
                adminBookingService.resolveDispute(104L, "REFUND_CUSTOMER", "Admin review", 99L));

        assertEquals(ErrorCode.BOOKING_003_INVALID_STATUS, ex.getErrorCode());
        verify(bookingRepository, never()).save(any());
        verify(depositRepository, never()).save(any());
    }

    @Test
    void resolveDispute_whenBookingInAcceptedStatus_throwsInvalidBookingExceptionWithConflictErrorCode() {
        User admin = new User();
        admin.setId(99L);
        admin.setRole(UserRole.ADMIN);
        when(userRepository.findById(99L)).thenReturn(Optional.of(admin));

        Booking booking = new Booking();
        booking.setId(105L);
        booking.setStatus(BookingStatus.ACCEPTED);
        when(bookingRepository.findByIdWithDetails(105L)).thenReturn(Optional.of(booking));

        InvalidBookingException ex = assertThrows(InvalidBookingException.class, () ->
                adminBookingService.resolveDispute(105L, "REFUND_CUSTOMER", "Admin review", 99L));

        assertEquals(ErrorCode.BOOKING_003_INVALID_STATUS, ex.getErrorCode());
        verify(bookingRepository, never()).save(any());
        verify(depositRepository, never()).save(any());
    }
}
