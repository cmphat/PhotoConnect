package com.photoconnect.service;

import com.photoconnect.dto.CustomerDashboardView;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.entity.Deposit;
import com.photoconnect.entity.DepositStatus;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.SavedPhotographer;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.exception.UnauthorizedException;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.DepositRepository;
import com.photoconnect.repository.PortfolioImageRepository;
import com.photoconnect.repository.ReviewRepository;
import com.photoconnect.repository.SavedPhotographerRepository;
import com.photoconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerDashboardServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private DepositRepository depositRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private SavedPhotographerRepository savedPhotographerRepository;
    @Mock private PortfolioImageRepository portfolioImageRepository;
    @Mock private DepositService depositService;

    private CustomerDashboardServiceImpl service;
    private User customer;
    private PhotographerProfile approvedProfile;

    @BeforeEach
    void setUp() {
        service = new CustomerDashboardServiceImpl(
                userRepository, bookingRepository, depositRepository, reviewRepository,
                savedPhotographerRepository, portfolioImageRepository, depositService);

        customer = user(100L, UserRole.CUSTOMER, UserStatus.ACTIVE, "Mai Nguyen");
        User photographer = user(200L, UserRole.PHOTOGRAPHER, UserStatus.ACTIVE, "An Tran");
        approvedProfile = new PhotographerProfile();
        approvedProfile.setId(10L);
        approvedProfile.setUser(photographer);
        approvedProfile.setDisplayName("An Studio");
        approvedProfile.setCity("Da Nang");
        approvedProfile.setCountry("Vietnam");
        approvedProfile.setVerificationStatus(PhotographerVerificationStatus.APPROVED);
        approvedProfile.setPriceFrom(new BigDecimal("3000000"));

        org.mockito.Mockito.lenient().when(userRepository.findById(100L)).thenReturn(Optional.of(customer));
        stubEmptyDashboardQueries();
    }

    @Test
    void summaryUsesCustomerScopedCounts() {
        when(bookingRepository.countUpcomingForCustomer(eq(100L), any(LocalDate.class), anyList())).thenReturn(2L);
        when(bookingRepository.countByCustomerIdAndStatus(100L, BookingStatus.PENDING)).thenReturn(1L);
        when(bookingRepository.countByCustomerIdAndStatus(100L, BookingStatus.COMPLETED)).thenReturn(7L);
        when(savedPhotographerRepository.countApprovedByCustomerId(100L)).thenReturn(3L);

        CustomerDashboardView dashboard = service.getDashboard(100L);

        assertThat(dashboard.getCustomerName()).isEqualTo("Mai Nguyen");
        assertThat(dashboard.getSummary().getUpcomingCount()).isEqualTo(2);
        assertThat(dashboard.getSummary().getPendingCount()).isEqualTo(1);
        assertThat(dashboard.getSummary().getCompletedCount()).isEqualTo(7);
        assertThat(dashboard.getSummary().getSavedCount()).isEqualTo(3);
        verify(bookingRepository).findDashboardAttentionCandidates(100L);
        verify(savedPhotographerRepository).findApprovedPreviewByCustomerId(eq(100L), any(Pageable.class));
    }

    @Test
    void acceptedBookingWithoutDepositShowsServerCalculatedPaymentAction() {
        Booking accepted = booking(501L, BookingStatus.ACCEPTED, LocalDate.now().plusDays(3));
        when(bookingRepository.findDashboardAttentionCandidates(100L)).thenReturn(List.of(accepted));
        when(depositService.calculateDepositAmount(new BigDecimal("3000000")))
                .thenReturn(new BigDecimal("900000.00"));

        CustomerDashboardView dashboard = service.getDashboard(100L);

        assertThat(dashboard.getAttentionItems()).hasSize(1);
        CustomerDashboardView.AttentionItem item = dashboard.getAttentionItems().get(0);
        assertThat(item.getCategory()).isEqualTo("Payment required");
        assertThat(item.getAmount()).isEqualByComparingTo("900000.00");
        assertThat(item.getAction().getPath()).isEqualTo("/bookings/501/deposit/checkout");
    }

    @Test
    void paidAcceptedBookingShowsUpcomingChatAndReceiptInsteadOfPayment() {
        Booking accepted = booking(502L, BookingStatus.ACCEPTED, LocalDate.now().plusDays(2));
        Deposit paid = deposit(accepted, DepositStatus.PAID, "900000.00");
        when(bookingRepository.findDashboardAttentionCandidates(100L)).thenReturn(List.of(accepted));
        when(bookingRepository.findDashboardBookings(eq(100L), any(LocalDate.class), anyList(), any(Pageable.class)))
                .thenReturn(List.of(accepted));
        when(depositRepository.findByBookingIdIn(anyList())).thenReturn(List.of(paid));
        when(reviewRepository.findReviewedBookingIds(anyList())).thenReturn(List.of());
        when(portfolioImageRepository.findDashboardImageCandidates(anyList())).thenReturn(List.of());

        CustomerDashboardView dashboard = service.getDashboard(100L);

        assertThat(dashboard.getAttentionItems()).extracting(CustomerDashboardView.AttentionItem::getCategory)
                .containsExactly("Upcoming session");
        assertThat(dashboard.getBookings().get(0).getActions())
                .extracting(CustomerDashboardView.Action::getLabel)
                .contains("View Receipt", "Open Chat")
                .doesNotContain("Pay Deposit", "Retry Payment");
    }

    @Test
    void completedBookingWithoutReviewIsEligibleForReview() {
        Booking completed = booking(503L, BookingStatus.COMPLETED, LocalDate.now().minusDays(1));
        when(bookingRepository.findDashboardAttentionCandidates(100L)).thenReturn(List.of(completed));
        when(reviewRepository.findReviewedBookingIds(anyList())).thenReturn(List.of());

        CustomerDashboardView dashboard = service.getDashboard(100L);

        assertThat(dashboard.getAttentionItems()).hasSize(1);
        assertThat(dashboard.getAttentionItems().get(0).getAction().getPath())
                .isEqualTo("/bookings/503/review");
    }

    @Test
    void completedBookingWithExistingReviewIsNotEligibleAgain() {
        Booking completed = booking(504L, BookingStatus.COMPLETED, LocalDate.now().minusDays(2));
        when(bookingRepository.findDashboardAttentionCandidates(100L)).thenReturn(List.of(completed));
        when(reviewRepository.findReviewedBookingIds(anyList())).thenReturn(List.of(504L));

        CustomerDashboardView dashboard = service.getDashboard(100L);

        assertThat(dashboard.getAttentionItems()).isEmpty();
    }

    @Test
    void savedPreviewIncludesOnlyCurrentlyApprovedProfiles() {
        SavedPhotographer approved = saved(1L, approvedProfile);
        PhotographerProfile unapprovedProfile = new PhotographerProfile();
        unapprovedProfile.setId(11L);
        unapprovedProfile.setDisplayName("Pending Studio");
        unapprovedProfile.setVerificationStatus(PhotographerVerificationStatus.PENDING);
        SavedPhotographer unapproved = saved(2L, unapprovedProfile);
        when(savedPhotographerRepository.findApprovedPreviewByCustomerId(eq(100L), any(Pageable.class)))
                .thenReturn(List.of(approved, unapproved));
        when(portfolioImageRepository.findDashboardImageCandidates(anyList())).thenReturn(List.of());

        CustomerDashboardView dashboard = service.getDashboard(100L);

        assertThat(dashboard.getSavedPhotographers()).hasSize(1);
        assertThat(dashboard.getSavedPhotographers().get(0).getPhotographerId()).isEqualTo(10L);
    }

    @Test
    void noBookingsAndNoSavedProfilesProducesEmptyCollectionsWithoutBulkQueries() {
        CustomerDashboardView dashboard = service.getDashboard(100L);

        assertThat(dashboard.getBookings()).isEmpty();
        assertThat(dashboard.getAttentionItems()).isEmpty();
        assertThat(dashboard.getSavedPhotographers()).isEmpty();
        verify(depositRepository, never()).findByBookingIdIn(anyList());
        verify(reviewRepository, never()).findReviewedBookingIds(anyList());
        verify(portfolioImageRepository, never()).findDashboardImageCandidates(anyList());
    }

    @Test
    void serviceRejectsNonCustomerEvenIfCalledOutsideController() {
        User photographer = user(200L, UserRole.PHOTOGRAPHER, UserStatus.ACTIVE, "Photographer");
        when(userRepository.findById(200L)).thenReturn(Optional.of(photographer));

        assertThatThrownBy(() -> service.getDashboard(200L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Only active customers");

        verify(bookingRepository, never()).findDashboardAttentionCandidates(200L);
    }

    private void stubEmptyDashboardQueries() {
        org.mockito.Mockito.lenient().when(bookingRepository.findDashboardBookings(eq(100L), any(LocalDate.class), anyList(), any(Pageable.class)))
                .thenReturn(List.of());
        org.mockito.Mockito.lenient().when(bookingRepository.findDashboardAttentionCandidates(100L)).thenReturn(List.of());
        org.mockito.Mockito.lenient().when(savedPhotographerRepository.findApprovedPreviewByCustomerId(eq(100L), any(Pageable.class)))
                .thenReturn(List.of());
    }

    private Booking booking(Long id, BookingStatus status, LocalDate date) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setCustomer(customer);
        booking.setPhotographerProfile(approvedProfile);
        booking.setBookingDate(date);
        booking.setBookingTime(LocalTime.of(14, 0));
        booking.setLocation("Da Nang Studio");
        booking.setAgreedPrice(new BigDecimal("3000000"));
        booking.setStatus(status);
        return booking;
    }

    private Deposit deposit(Booking booking, DepositStatus status, String amount) {
        Deposit deposit = new Deposit(booking, new BigDecimal(amount));
        deposit.setStatus(status);
        return deposit;
    }

    private SavedPhotographer saved(Long id, PhotographerProfile profile) {
        SavedPhotographer saved = new SavedPhotographer(customer, profile);
        saved.setId(id);
        return saved;
    }

    private User user(Long id, UserRole role, UserStatus status, String fullName) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setStatus(status);
        user.setFullName(fullName);
        return user;
    }
}
