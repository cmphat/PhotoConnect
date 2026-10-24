package com.photoconnect.service;

import com.photoconnect.dto.BookingHistoryDto;
import com.photoconnect.entity.*;
import com.photoconnect.exception.UnauthorizedException;
import com.photoconnect.repository.BookingHistoryRepository;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.DepositRepository;
import com.photoconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.mockito.ArgumentCaptor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingStateMachineServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingHistoryRepository bookingHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepositRepository depositRepository;

    @InjectMocks
    private BookingStateMachineServiceImpl stateMachineService;

    private User customer;
    private User photographerUser;
    private User stranger;
    private PhotographerProfile photographerProfile;
    private Booking booking;

    @BeforeEach
    void setUp() {
        customer = new User();
        customer.setId(10L);
        customer.setFullName("Customer Alice");
        customer.setRole(UserRole.CUSTOMER);

        photographerUser = new User();
        photographerUser.setId(20L);
        photographerUser.setFullName("Photographer Bob");
        photographerUser.setRole(UserRole.PHOTOGRAPHER);

        stranger = new User();
        stranger.setId(99L);
        stranger.setFullName("Stranger Dan");
        stranger.setRole(UserRole.CUSTOMER);

        photographerProfile = new PhotographerProfile();
        photographerProfile.setId(100L);
        photographerProfile.setUser(photographerUser);

        booking = new Booking();
        booking.setId(500L);
        booking.setCustomer(customer);
        booking.setPhotographerProfile(photographerProfile);
        booking.setStatus(BookingStatus.PENDING);
        booking.setAgreedPrice(BigDecimal.valueOf(2500000));
        booking.setBookingDate(LocalDate.now().plusDays(5));
        booking.setBookingTime(LocalTime.of(10, 0));
        booking.setDurationMinutes(120);
    }

    @Test
    void transition_PendingToAccepted_Photographer_Success() {
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(20L)).thenReturn(Optional.of(photographerUser));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking updated = stateMachineService.transition(500L, BookingStatus.ACCEPTED, 20L, "Accepting client request");

        assertThat(updated.getStatus()).isEqualTo(BookingStatus.ACCEPTED);
        verify(bookingHistoryRepository, times(1)).save(any(BookingHistory.class));
    }

    @Test
    void transition_PendingToAccepted_Customer_ThrowsUnauthorized() {
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(10L)).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> stateMachineService.transition(500L, BookingStatus.ACCEPTED, 10L, "Customer cannot accept"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Only the assigned photographer");

        verify(bookingRepository, never()).save(any());
        verify(bookingHistoryRepository, never()).save(any());
    }

    @Test
    void transition_AcceptedToConfirmed_WithPaidDeposit_Success() {
        booking.setStatus(BookingStatus.ACCEPTED);

        Deposit deposit = new Deposit();
        deposit.setId(1L);
        deposit.setBooking(booking);
        deposit.setStatus(DepositStatus.PAID);
        deposit.setPaidAt(LocalDateTime.now());

        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(10L)).thenReturn(Optional.of(customer));
        when(depositRepository.findByBookingIdWithDetails(500L)).thenReturn(Optional.of(deposit));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking updated = stateMachineService.transition(500L, BookingStatus.CONFIRMED, 10L, "Deposit paid via QR");

        assertThat(updated.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(bookingHistoryRepository, times(1)).save(any(BookingHistory.class));
    }

    @Test
    void transition_AcceptedToConfirmed_WithoutPaidDeposit_ThrowsIllegalStateException() {
        booking.setStatus(BookingStatus.ACCEPTED);

        Deposit pendingDeposit = new Deposit();
        pendingDeposit.setStatus(DepositStatus.PENDING);

        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(10L)).thenReturn(Optional.of(customer));
        when(depositRepository.findByBookingIdWithDetails(500L)).thenReturn(Optional.of(pendingDeposit));

        assertThatThrownBy(() -> stateMachineService.transition(500L, BookingStatus.CONFIRMED, 10L, "Attempt to confirm without payment"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot confirm booking without a confirmed PAID deposit payment");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void transition_LifecycleProgress_ConfirmedToShootingToPostProcessingToDeliveredToCompleted() {
        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(20L)).thenReturn(Optional.of(photographerUser));
        when(userRepository.findById(10L)).thenReturn(Optional.of(customer));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        // 1. Confirmed -> Shooting
        Booking s1 = stateMachineService.transition(500L, BookingStatus.SHOOTING, 20L, "Shoot session started");
        assertThat(s1.getStatus()).isEqualTo(BookingStatus.SHOOTING);

        // 2. Shooting -> Post Processing
        Booking s2 = stateMachineService.transition(500L, BookingStatus.POST_PROCESSING, 20L, "Shoot wrapped, editing raw files");
        assertThat(s2.getStatus()).isEqualTo(BookingStatus.POST_PROCESSING);

        // 3. Post Processing -> Delivered
        Booking s3 = stateMachineService.transition(500L, BookingStatus.DELIVERED, 20L, "Uploaded delivery gallery");
        assertThat(s3.getStatus()).isEqualTo(BookingStatus.DELIVERED);

        // 4. Delivered -> Completed (by customer)
        Booking s4 = stateMachineService.transition(500L, BookingStatus.COMPLETED, 10L, "Client confirmed receipt");
        assertThat(s4.getStatus()).isEqualTo(BookingStatus.COMPLETED);

        verify(bookingHistoryRepository, times(4)).save(any(BookingHistory.class));
    }

    @Test
    void transition_IdempotentRetry_DoesNotDuplicateTransition() {
        booking.setStatus(BookingStatus.ACCEPTED);

        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(20L)).thenReturn(Optional.of(photographerUser));

        Booking result = stateMachineService.transition(500L, BookingStatus.ACCEPTED, 20L, "Retry accept");

        assertThat(result.getStatus()).isEqualTo(BookingStatus.ACCEPTED);
        verify(bookingRepository, never()).save(any());
        verify(bookingHistoryRepository, never()).save(any());
    }

    @Test
    void transition_InvalidTransition_ThrowsIllegalStateException() {
        booking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(20L)).thenReturn(Optional.of(photographerUser));

        // Direct leap from PENDING to COMPLETED is forbidden
        assertThatThrownBy(() -> stateMachineService.transition(500L, BookingStatus.COMPLETED, 20L, "Skip everything"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid transition: cannot move booking from PENDING to COMPLETED");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void transition_UnauthorizedActor_StrangerCancels_ThrowsUnauthorized() {
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(99L)).thenReturn(Optional.of(stranger));

        assertThatThrownBy(() -> stateMachineService.transition(500L, BookingStatus.CANCELLED, 99L, "Stranger malicious cancel"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Only the booking customer or photographer can cancel");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void transition_Shooting_ByAnotherPhotographer_ThrowsUnauthorized() {
        booking.setStatus(BookingStatus.CONFIRMED);
        User anotherPhotographer = new User();
        anotherPhotographer.setId(77L);
        anotherPhotographer.setFullName("Photographer Eve");
        anotherPhotographer.setRole(UserRole.PHOTOGRAPHER);

        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(77L)).thenReturn(Optional.of(anotherPhotographer));

        assertThatThrownBy(() -> stateMachineService.transition(500L, BookingStatus.SHOOTING, 77L, "Malicious attempt"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Only the assigned photographer can perform this action");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void transition_PostProcessing_ByCustomer_ThrowsUnauthorized() {
        booking.setStatus(BookingStatus.SHOOTING);

        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(10L)).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> stateMachineService.transition(500L, BookingStatus.POST_PROCESSING, 10L, "Customer trying to advance editing"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Only the assigned photographer can perform this action");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void transition_Delivered_ByAnotherPhotographer_ThrowsUnauthorized() {
        booking.setStatus(BookingStatus.POST_PROCESSING);
        User anotherPhotographer = new User();
        anotherPhotographer.setId(77L);
        anotherPhotographer.setRole(UserRole.PHOTOGRAPHER);

        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(77L)).thenReturn(Optional.of(anotherPhotographer));

        assertThatThrownBy(() -> stateMachineService.transition(500L, BookingStatus.DELIVERED, 77L, "Malicious delivery"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Only the assigned photographer can perform this action");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void getHistoryForBooking_AuthorizedParticipants_ReturnsList() {
        BookingHistory h = new BookingHistory(booking, BookingStatus.PENDING, BookingStatus.ACCEPTED, photographerUser, "Approved");
        h.setId(101L);

        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(10L)).thenReturn(Optional.of(customer));
        when(bookingHistoryRepository.findByBookingIdWithUserOrderByChangedAtAsc(500L))
                .thenReturn(List.of(h));

        List<BookingHistoryDto> history = stateMachineService.getHistoryForBooking(500L, 10L);

        assertThat(history).hasSize(1);
        assertThat(history.get(0).getOldStatus()).isEqualTo("PENDING");
        assertThat(history.get(0).getNewStatus()).isEqualTo("ACCEPTED");
        assertThat(history.get(0).getChangedByUserName()).isEqualTo("Photographer Bob");
    }

    @Test
    void getHistoryForBooking_Stranger_ThrowsUnauthorized() {
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(99L)).thenReturn(Optional.of(stranger));

        assertThatThrownBy(() -> stateMachineService.getHistoryForBooking(500L, 99L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("You are not authorized to view the history");
    }

    @Test
    void transition_CapturesAccurateBookingHistory_WithAllRequiredAuditFields() {
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(20L)).thenReturn(Optional.of(photographerUser));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        stateMachineService.transition(500L, BookingStatus.ACCEPTED, 20L, "Detailed acceptance note");

        ArgumentCaptor<BookingHistory> captor = ArgumentCaptor.forClass(BookingHistory.class);
        verify(bookingHistoryRepository).save(captor.capture());

        BookingHistory savedHistory = captor.getValue();
        assertThat(savedHistory.getBooking().getId()).isEqualTo(500L);
        assertThat(savedHistory.getOldStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(savedHistory.getNewStatus()).isEqualTo(BookingStatus.ACCEPTED);
        assertThat(savedHistory.getChangedBy().getId()).isEqualTo(20L);
        assertThat(savedHistory.getChangedBy().getRole()).isEqualTo(UserRole.PHOTOGRAPHER);
        assertThat(savedHistory.getReason()).isEqualTo("Detailed acceptance note");
        assertThat(savedHistory.getChangedAt()).isNotNull();
        assertThat(savedHistory.getChangedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void transition_PendingToRejected_GeneratesExactOneHistoryRecord() {
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(20L)).thenReturn(Optional.of(photographerUser));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        stateMachineService.transition(500L, BookingStatus.REJECTED, 20L, "Schedule fully booked");

        ArgumentCaptor<BookingHistory> captor = ArgumentCaptor.forClass(BookingHistory.class);
        verify(bookingHistoryRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getOldStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(captor.getValue().getNewStatus()).isEqualTo(BookingStatus.REJECTED);
        assertThat(captor.getValue().getReason()).isEqualTo("Schedule fully booked");
    }

    @Test
    void transition_PendingToCancelled_GeneratesExactOneHistoryRecord() {
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(10L)).thenReturn(Optional.of(customer));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        stateMachineService.transition(500L, BookingStatus.CANCELLED, 10L, "Changed plans");

        ArgumentCaptor<BookingHistory> captor = ArgumentCaptor.forClass(BookingHistory.class);
        verify(bookingHistoryRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getOldStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(captor.getValue().getNewStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(captor.getValue().getChangedBy().getId()).isEqualTo(10L);
    }

    @Test
    void transition_DisputedTransitions_ByAdmin_GeneratesExactOneHistoryRecord() {
        User admin = new User();
        admin.setId(1L);
        admin.setFullName("Admin Root");
        admin.setRole(UserRole.ADMIN);

        booking.setStatus(BookingStatus.DISPUTED);
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        stateMachineService.transition(500L, BookingStatus.COMPLETED, 1L, "Admin resolved dispute in favor of photographer");

        ArgumentCaptor<BookingHistory> captor = ArgumentCaptor.forClass(BookingHistory.class);
        verify(bookingHistoryRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getOldStatus()).isEqualTo(BookingStatus.DISPUTED);
        assertThat(captor.getValue().getNewStatus()).isEqualTo(BookingStatus.COMPLETED);
        assertThat(captor.getValue().getChangedBy().getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void transition_DeliveredToPostProcessing_ByCustomer_Success() {
        booking.setStatus(BookingStatus.DELIVERED);
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(10L)).thenReturn(Optional.of(customer));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking result = stateMachineService.transition(500L, BookingStatus.POST_PROCESSING, 10L, "Client requested color adjustments");

        assertThat(result.getStatus()).isEqualTo(BookingStatus.POST_PROCESSING);
        ArgumentCaptor<BookingHistory> captor = ArgumentCaptor.forClass(BookingHistory.class);
        verify(bookingHistoryRepository).save(captor.capture());
        assertThat(captor.getValue().getOldStatus()).isEqualTo(BookingStatus.DELIVERED);
        assertThat(captor.getValue().getNewStatus()).isEqualTo(BookingStatus.POST_PROCESSING);
        assertThat(captor.getValue().getChangedBy().getId()).isEqualTo(10L);
        assertThat(captor.getValue().getReason()).isEqualTo("Client requested color adjustments");
    }

    @Test
    void transition_DeliveredToPostProcessing_ByStranger_ThrowsUnauthorized() {
        booking.setStatus(BookingStatus.DELIVERED);
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(99L)).thenReturn(Optional.of(stranger));

        assertThatThrownBy(() -> stateMachineService.transition(500L, BookingStatus.POST_PROCESSING, 99L, "Malicious revision request"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Only the booking customer or assigned photographer can return delivery to post-processing");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void transition_DeliveredToCompleted_ByPhotographer_ThrowsUnauthorized() {
        booking.setStatus(BookingStatus.DELIVERED);
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(20L)).thenReturn(Optional.of(photographerUser));

        assertThatThrownBy(() -> stateMachineService.transition(500L, BookingStatus.COMPLETED, 20L, "Photographer trying to force complete"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Only the customer can accept delivery and complete the booking");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void transition_DeliveredToCompleted_ByCustomer_Success() {
        booking.setStatus(BookingStatus.DELIVERED);
        when(bookingRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(10L)).thenReturn(Optional.of(customer));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking result = stateMachineService.transition(500L, BookingStatus.COMPLETED, 10L, "Customer confirmed receipt");

        assertThat(result.getStatus()).isEqualTo(BookingStatus.COMPLETED);
        verify(bookingHistoryRepository).save(any(BookingHistory.class));
    }
}
