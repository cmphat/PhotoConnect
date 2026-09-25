package com.photoconnect.service;

import com.photoconnect.dto.PhotographerDashboardView;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.entity.Deposit;
import com.photoconnect.entity.DepositStatus;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerUnavailableDate;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.PortfolioCategory;
import com.photoconnect.entity.PortfolioImage;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.exception.UnauthorizedException;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.DepositRepository;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.repository.PhotographerUnavailableDateRepository;
import com.photoconnect.repository.PortfolioImageRepository;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhotographerDashboardServiceTest {

    @Mock private PhotographerProfileRepository profileRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private DepositRepository depositRepository;
    @Mock private PortfolioImageRepository portfolioImageRepository;
    @Mock private PhotographerUnavailableDateRepository unavailableDateRepository;
    @Mock private PhotographerProfileService photographerProfileService;

    private PhotographerDashboardServiceImpl service;
    private PhotographerProfile profile;
    private User customer;

    @BeforeEach
    void setUp() {
        service = new PhotographerDashboardServiceImpl(
                profileRepository, bookingRepository, depositRepository,
                portfolioImageRepository, unavailableDateRepository, photographerProfileService);

        User photographer = user(100L, UserRole.PHOTOGRAPHER, UserStatus.ACTIVE, "An Tran");
        customer = user(200L, UserRole.CUSTOMER, UserStatus.ACTIVE, "Mai Nguyen");
        profile = new PhotographerProfile();
        profile.setId(10L);
        profile.setUser(photographer);
        profile.setDisplayName("An Studio");
        profile.setVerificationStatus(PhotographerVerificationStatus.APPROVED);
        profile.setAverageRating(4.75);
        profile.setReviewCount(8);

        lenient().when(profileRepository.findByUserIdWithUser(100L)).thenReturn(Optional.of(profile));
        stubEmptyScopedQueries();
    }

    @Test
    void emptyPhotographerStateUsesRealProfileAndOnboardingAttention() {
        when(photographerProfileService.calculateProfileCompleteness(profile, 0, false)).thenReturn(25);

        PhotographerDashboardView dashboard = service.getDashboard(100L);

        assertThat(dashboard.getProfessionalName()).isEqualTo("An Studio");
        assertThat(dashboard.getBookingRequests()).isEmpty();
        assertThat(dashboard.getUpcomingShoots()).isEmpty();
        assertThat(dashboard.getPortfolioPreview()).isEmpty();
        assertThat(dashboard.getAttentionItems())
                .extracting(PhotographerDashboardView.AttentionItem::getType)
                .containsExactly("PROFILE_INCOMPLETE", "PORTFOLIO_EMPTY");
        assertThat(dashboard.getRatingSummary().getAverageRating()).isEqualTo(4.75);
        assertThat(dashboard.getRatingSummary().getReviewCount()).isEqualTo(8);
    }

    @Test
    void profileCompletenessDelegatesToC01Formula() {
        when(portfolioImageRepository.countByPhotographerProfileId(10L)).thenReturn(3L);
        when(portfolioImageRepository.findFirstByPhotographerProfileIdAndIsCoverTrue(10L))
                .thenReturn(Optional.of(portfolioImage(1L, true, PortfolioCategory.PORTRAIT)));
        when(photographerProfileService.calculateProfileCompleteness(profile, 3, true)).thenReturn(90);

        PhotographerDashboardView dashboard = service.getDashboard(100L);

        assertThat(dashboard.getProfileCompleteness()).isEqualTo(90);
        verify(photographerProfileService).calculateProfileCompleteness(profile, 3, true);
    }

    @Test
    void pendingRequestIsActionableButNonPendingDefensiveResultIsNot() {
        Booking pending = booking(501L, BookingStatus.PENDING, LocalDate.now().plusDays(10));
        Booking accepted = booking(502L, BookingStatus.ACCEPTED, LocalDate.now().plusDays(10));
        when(bookingRepository.findPendingStudioRequests(eq(10L), any(Pageable.class)))
                .thenReturn(List.of(pending, accepted));
        when(bookingRepository.countByPhotographerProfileIdAndStatus(10L, BookingStatus.PENDING))
                .thenReturn(1L);

        PhotographerDashboardView dashboard = service.getDashboard(100L);

        assertThat(dashboard.getBookingRequests()).singleElement()
                .satisfies(card -> {
                    assertThat(card.getId()).isEqualTo(501L);
                    assertThat(card.getActions())
                            .extracting(PhotographerDashboardView.Action::getLabel)
                            .containsExactly("Review", "Accept", "Reject");
                });
        assertThat(dashboard.getAttentionItems())
                .filteredOn(item -> item.getType().equals("BOOKING_REQUEST"))
                .singleElement()
                .satisfies(item -> assertThat(item.getAction().getPath())
                        .isEqualTo("/photographer/bookings/501"));
    }

    @Test
    void upcomingShootAndDepositsAreScopedAndReadOnly() {
        Booking paidBooking = booking(601L, BookingStatus.ACCEPTED, LocalDate.now().plusDays(2));
        Booking unpaidBooking = booking(602L, BookingStatus.ACCEPTED, LocalDate.now().plusDays(4));
        Deposit paid = new Deposit(paidBooking, new BigDecimal("900000"));
        paid.setStatus(DepositStatus.PAID);
        when(bookingRepository.findUpcomingAcceptedForStudio(eq(10L), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(List.of(paidBooking, unpaidBooking));
        when(depositRepository.findByBookingIdIn(List.of(601L, 602L))).thenReturn(List.of(paid));

        PhotographerDashboardView dashboard = service.getDashboard(100L);

        assertThat(dashboard.getUpcomingShoots())
                .extracting(PhotographerDashboardView.UpcomingShootCard::getDepositLabel)
                .containsExactly("Deposit Paid", "Awaiting Customer Deposit");
        assertThat(dashboard.getUpcomingShoots()).allSatisfy(shoot ->
                assertThat(shoot.getActions())
                        .allSatisfy(action -> assertThat(action.getMethod()).isEqualTo("GET"))
                        .extracting(PhotographerDashboardView.Action::getPath)
                        .allMatch(path -> !path.contains("deposit")));
        verify(bookingRepository).findUpcomingAcceptedForStudio(eq(10L), any(LocalDate.class), any(Pageable.class));
    }

    @Test
    void portfolioSummaryPreviewAvailabilityAndRatingUseProfileScope() {
        PortfolioImage cover = portfolioImage(701L, true, PortfolioCategory.WEDDING);
        PortfolioImage portrait = portfolioImage(702L, false, PortfolioCategory.PORTRAIT);
        when(portfolioImageRepository.countByPhotographerProfileId(10L)).thenReturn(2L);
        when(portfolioImageRepository.countDistinctCategoriesByProfileId(10L)).thenReturn(2L);
        when(portfolioImageRepository.findFirstByPhotographerProfileIdAndIsCoverTrue(10L))
                .thenReturn(Optional.of(cover));
        when(portfolioImageRepository.findStudioPreview(eq(10L), any(Pageable.class)))
                .thenReturn(List.of(cover, portrait));
        when(photographerProfileService.calculateProfileCompleteness(profile, 2, true)).thenReturn(100);
        LocalDate nextBlocked = LocalDate.now().plusDays(3);
        PhotographerUnavailableDate unavailable = new PhotographerUnavailableDate(profile, nextBlocked, "Travel");
        when(unavailableDateRepository.countByPhotographerProfileIdAndDateGreaterThanEqual(eq(10L), any(LocalDate.class)))
                .thenReturn(2L);
        when(unavailableDateRepository.findFirstByPhotographerProfileIdAndDateGreaterThanEqualOrderByDateAsc(
                eq(10L), any(LocalDate.class))).thenReturn(Optional.of(unavailable));

        PhotographerDashboardView dashboard = service.getDashboard(100L);

        assertThat(dashboard.getPortfolioSummary().getImageCount()).isEqualTo(2);
        assertThat(dashboard.getPortfolioSummary().getCategoryCount()).isEqualTo(2);
        assertThat(dashboard.getPortfolioSummary().getHasCover()).isTrue();
        assertThat(dashboard.getPortfolioPreview()).hasSize(2)
                .extracting(PhotographerDashboardView.PortfolioPreview::getThumbnailUrl)
                .allMatch(url -> url.contains("c_fill,w_600,h_750,q_auto,f_auto"));
        assertThat(dashboard.getAvailabilitySummary().getUpcomingBlockedCount()).isEqualTo(2);
        assertThat(dashboard.getAvailabilitySummary().getNextUnavailableDate()).isEqualTo(nextBlocked);
        assertThat(dashboard.getRatingSummary().getReviewCount()).isEqualTo(8);
    }

    @Test
    void photographerAQueriesOnlyProfileADespiteOtherProfileExisting() {
        PhotographerProfile other = new PhotographerProfile();
        other.setId(20L);

        service.getDashboard(100L);

        verify(profileRepository).findByUserIdWithUser(100L);
        verify(bookingRepository).findPendingStudioRequests(eq(10L), any(Pageable.class));
        verify(bookingRepository).findUpcomingAcceptedForStudio(eq(10L), any(LocalDate.class), any(Pageable.class));
        verify(portfolioImageRepository).findStudioPreview(eq(10L), any(Pageable.class));
        verify(bookingRepository, never()).findPendingStudioRequests(eq(20L), any(Pageable.class));
        verify(portfolioImageRepository, never()).findStudioPreview(eq(20L), any(Pageable.class));
    }

    @Test
    void serviceRejectsDatabaseUserWhoseRoleIsNotPhotographer() {
        profile.getUser().setRole(UserRole.CUSTOMER);

        assertThatThrownBy(() -> service.getDashboard(100L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Only active photographers");
        verify(bookingRepository, never()).findPendingStudioRequests(any(), any());
    }

    private void stubEmptyScopedQueries() {
        lenient().when(portfolioImageRepository.countByPhotographerProfileId(10L)).thenReturn(0L);
        lenient().when(portfolioImageRepository.countDistinctCategoriesByProfileId(10L)).thenReturn(0L);
        lenient().when(portfolioImageRepository.findFirstByPhotographerProfileIdAndIsCoverTrue(10L))
                .thenReturn(Optional.empty());
        lenient().when(portfolioImageRepository.findStudioPreview(eq(10L), any(Pageable.class)))
                .thenReturn(List.of());
        lenient().when(photographerProfileService.calculateProfileCompleteness(profile, 0, false)).thenReturn(25);
        lenient().when(bookingRepository.findPendingStudioRequests(eq(10L), any(Pageable.class)))
                .thenReturn(List.of());
        lenient().when(bookingRepository.countByPhotographerProfileIdAndStatus(10L, BookingStatus.PENDING))
                .thenReturn(0L);
        lenient().when(bookingRepository.findUpcomingAcceptedForStudio(eq(10L), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(List.of());
        lenient().when(unavailableDateRepository.countByPhotographerProfileIdAndDateGreaterThanEqual(
                eq(10L), any(LocalDate.class))).thenReturn(0L);
        lenient().when(unavailableDateRepository.findFirstByPhotographerProfileIdAndDateGreaterThanEqualOrderByDateAsc(
                eq(10L), any(LocalDate.class))).thenReturn(Optional.empty());
    }

    private Booking booking(Long id, BookingStatus status, LocalDate date) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setCustomer(customer);
        booking.setPhotographerProfile(profile);
        booking.setBookingDate(date);
        booking.setBookingTime(LocalTime.of(14, 30));
        booking.setLocation("Da Nang Studio");
        booking.setNotes("Editorial portrait session with two looks.");
        booking.setAgreedPrice(new BigDecimal("3000000"));
        booking.setStatus(status);
        return booking;
    }

    private PortfolioImage portfolioImage(Long id, boolean cover, PortfolioCategory category) {
        PortfolioImage image = new PortfolioImage();
        image.setId(id);
        image.setPhotographerProfile(profile);
        image.setImageUrl("https://res.cloudinary.com/demo/image/upload/v1/photo-" + id + ".jpg");
        image.setCategory(category);
        image.setCover(cover);
        return image;
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
