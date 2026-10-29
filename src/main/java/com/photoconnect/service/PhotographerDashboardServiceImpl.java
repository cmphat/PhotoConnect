package com.photoconnect.service;

import com.photoconnect.dto.PhotographerAnalyticsDto;
import com.photoconnect.dto.NotificationDto;
import com.photoconnect.dto.PhotographerDashboardView;
import com.photoconnect.entity.*;
import com.photoconnect.exception.UnauthorizedException;
import com.photoconnect.exception.PhotographerStudioProfileNotFoundException;
import com.photoconnect.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class PhotographerDashboardServiceImpl implements PhotographerDashboardService {

    private static final int REQUEST_PREVIEW_LIMIT = 4;
    private static final int SHOOT_PREVIEW_LIMIT = 4;
    private static final int PORTFOLIO_PREVIEW_LIMIT = 6;
    private static final int ATTENTION_LIMIT = 5;
    private static final int IMMINENT_SHOOT_DAYS = 7;

    private final PhotographerProfileRepository photographerProfileRepository;
    private final BookingRepository bookingRepository;
    private final DepositRepository depositRepository;
    private final PortfolioImageRepository portfolioImageRepository;
    private final PhotographerUnavailableDateRepository unavailableDateRepository;
    private final PhotographerProfileService photographerProfileService;
    private final NotificationRepository notificationRepository;

    public PhotographerDashboardServiceImpl(
            PhotographerProfileRepository photographerProfileRepository,
            BookingRepository bookingRepository,
            DepositRepository depositRepository,
            PortfolioImageRepository portfolioImageRepository,
            PhotographerUnavailableDateRepository unavailableDateRepository,
            PhotographerProfileService photographerProfileService) {
        this(photographerProfileRepository, bookingRepository, depositRepository,
             portfolioImageRepository, unavailableDateRepository, photographerProfileService, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public PhotographerDashboardServiceImpl(
            PhotographerProfileRepository photographerProfileRepository,
            BookingRepository bookingRepository,
            DepositRepository depositRepository,
            PortfolioImageRepository portfolioImageRepository,
            PhotographerUnavailableDateRepository unavailableDateRepository,
            PhotographerProfileService photographerProfileService,
            @org.springframework.beans.factory.annotation.Autowired(required = false) NotificationRepository notificationRepository) {
        this.photographerProfileRepository = photographerProfileRepository;
        this.bookingRepository = bookingRepository;
        this.depositRepository = depositRepository;
        this.portfolioImageRepository = portfolioImageRepository;
        this.unavailableDateRepository = unavailableDateRepository;
        this.photographerProfileService = photographerProfileService;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public PhotographerDashboardView getDashboard(Long photographerUserId) {
        PhotographerProfile profile = requireActivePhotographer(photographerUserId);
        Long profileId = profile.getId();
        LocalDate today = LocalDate.now();

        long portfolioCount = portfolioImageRepository.countByPhotographerProfileId(profileId);
        boolean hasCover = portfolioImageRepository
                .findFirstByPhotographerProfileIdAndIsCoverTrue(profileId).isPresent();
        long categoryCount = portfolioImageRepository.countDistinctCategoriesByProfileId(profileId);
        int completeness = photographerProfileService.calculateProfileCompleteness(
                profile, Math.toIntExact(Math.min(portfolioCount, Integer.MAX_VALUE)), hasCover);

        List<Booking> pendingRequests = bookingRepository.findPendingStudioRequests(
                        profileId, PageRequest.of(0, REQUEST_PREVIEW_LIMIT)).stream()
                .filter(booking -> booking.getStatus() == BookingStatus.PENDING)
                .toList();
        long pendingRequestCount = bookingRepository.countByPhotographerProfileIdAndStatus(
                profileId, BookingStatus.PENDING);

        List<Booking> upcomingBookings = bookingRepository.findUpcomingAcceptedForStudio(
                        profileId, today, PageRequest.of(0, SHOOT_PREVIEW_LIMIT)).stream()
                .filter(booking -> booking.getStatus() == BookingStatus.ACCEPTED
                        && !booking.getBookingDate().isBefore(today))
                .toList();
        Map<Long, Deposit> deposits = loadDeposits(upcomingBookings);

        List<PortfolioImage> previewImages = portfolioImageRepository.findStudioPreview(
                profileId, PageRequest.of(0, PORTFOLIO_PREVIEW_LIMIT));
        long blockedCount = unavailableDateRepository
                .countByPhotographerProfileIdAndDateGreaterThanEqual(profileId, today);
        LocalDate nextUnavailable = unavailableDateRepository
                .findFirstByPhotographerProfileIdAndDateGreaterThanEqualOrderByDateAsc(profileId, today)
                .map(PhotographerUnavailableDate::getDate)
                .orElse(null);

        List<PhotographerDashboardView.BookingRequestCard> requestCards = pendingRequests.stream()
                .map(this::toRequestCard)
                .toList();
        List<PhotographerDashboardView.UpcomingShootCard> shootCards = upcomingBookings.stream()
                .map(booking -> toUpcomingShootCard(booking, deposits.get(booking.getId())))
                .toList();
        List<PhotographerDashboardView.PortfolioPreview> portfolioPreview = previewImages.stream()
                .map(image -> toPortfolioPreview(image, profile.getDisplayName()))
                .toList();

        return new PhotographerDashboardView(
                profileId,
                professionalName(profile),
                profile.getVerificationStatus(),
                profile.getVerificationStatus() == PhotographerVerificationStatus.APPROVED,
                completeness,
                buildAttentionItems(pendingRequests, pendingRequestCount, upcomingBookings,
                        completeness, portfolioCount, hasCover, today),
                requestCards,
                shootCards,
                new PhotographerDashboardView.PortfolioSummary(
                        portfolioCount, categoryCount, hasCover),
                portfolioPreview,
                new PhotographerDashboardView.AvailabilitySummary(blockedCount, nextUnavailable),
                new PhotographerDashboardView.RatingSummary(
                        profile.getAverageRating() != null ? profile.getAverageRating() : 0.0,
                        profile.getReviewCount() != null ? profile.getReviewCount() : 0)
        );
    }

    @Override
    public PhotographerAnalyticsDto getAnalytics(Long photographerUserId) {
        PhotographerProfile profile = requireActivePhotographer(photographerUserId);
        Long profileId = profile.getId();
        LocalDate today = LocalDate.now();

        PhotographerAnalyticsDto dto = new PhotographerAnalyticsDto();

        // 1. Booking counts
        dto.setTotalBookings(bookingRepository.countByPhotographerProfileId(profileId));
        dto.setPendingRequests(bookingRepository.countByPhotographerProfileIdAndStatus(profileId, BookingStatus.PENDING));
        dto.setUpcomingSessions(bookingRepository.countUpcomingAcceptedForStudio(profileId, today));
        dto.setCompletedBookings(bookingRepository.countByPhotographerProfileIdAndStatus(profileId, BookingStatus.COMPLETED));
        dto.setCancelledBookings(bookingRepository.countByPhotographerProfileIdAndStatus(profileId, BookingStatus.CANCELLED));

        // 2. Financial Metrics (distinct accounting states)
        dto.setGrossBookingValue(bookingRepository.calculateGrossBookingValue(profileId));
        dto.setConfirmedDeposits(depositRepository.calculateConfirmedDepositsForPhotographer(profileId));
        dto.setCompletedRevenue(bookingRepository.calculateCompletedRevenue(profileId));

        // 3. Ratings
        dto.setAverageRating(profile.getAverageRating() != null ? profile.getAverageRating() : 0.0);
        dto.setReviewCount(profile.getReviewCount() != null ? profile.getReviewCount() : 0);

        // 4. Status distribution
        Map<String, Long> statusMap = new LinkedHashMap<>();
        for (BookingStatus status : BookingStatus.values()) {
            statusMap.put(status.name(), 0L);
        }
        List<Object[]> statusCounts = bookingRepository.countBookingsByStatusForPhotographer(profileId);
        if (statusCounts != null) {
            for (Object[] row : statusCounts) {
                if (row != null && row.length >= 2 && row[0] != null) {
                    BookingStatus status = (BookingStatus) row[0];
                    Long count = ((Number) row[1]).longValue();
                    statusMap.put(status.name(), count);
                }
            }
        }
        dto.setStatusDistribution(statusMap);

        // 5. Popular packages
        List<PhotographerAnalyticsDto.PackageStatDto> packageStats = new ArrayList<>();
        List<Object[]> popularRows = bookingRepository.findPopularPackagesForPhotographer(profileId);
        if (popularRows != null) {
            for (Object[] row : popularRows) {
                if (row != null && row.length >= 3) {
                    String pkgName = row[0] != null ? row[0].toString() : "Custom Session";
                    long count = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                    BigDecimal rev = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;
                    packageStats.add(new PhotographerAnalyticsDto.PackageStatDto(pkgName, count, rev));
                }
            }
        }
        dto.setPopularPackages(packageStats);

        // 6. Monthly trends (past 6 months up to current month)
        LocalDate startMonthDate = today.minusMonths(5).withDayOfMonth(1);
        List<Object[]> trendRows = bookingRepository.findBookingTrendsSince(profileId, startMonthDate);

        Map<YearMonth, Long> countMap = new LinkedHashMap<>();
        Map<YearMonth, BigDecimal> grossMap = new LinkedHashMap<>();
        Map<YearMonth, BigDecimal> completedMap = new LinkedHashMap<>();
        YearMonth curYm = YearMonth.from(startMonthDate);
        YearMonth endYm = YearMonth.from(today);
        while (!curYm.isAfter(endYm)) {
            countMap.put(curYm, 0L);
            grossMap.put(curYm, BigDecimal.ZERO);
            completedMap.put(curYm, BigDecimal.ZERO);
            curYm = curYm.plusMonths(1);
        }

        if (trendRows != null) {
            for (Object[] row : trendRows) {
                if (row != null && row.length >= 3 && row[0] != null) {
                    LocalDate bDate = (LocalDate) row[0];
                    BookingStatus bStatus = (BookingStatus) row[1];
                    BigDecimal price = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;
                    YearMonth ym = YearMonth.from(bDate);

                    if (countMap.containsKey(ym)) {
                        countMap.put(ym, countMap.get(ym) + 1);
                        if (bStatus != BookingStatus.CANCELLED && bStatus != BookingStatus.REJECTED) {
                            grossMap.put(ym, grossMap.get(ym).add(price));
                        }
                        if (bStatus == BookingStatus.COMPLETED) {
                            completedMap.put(ym, completedMap.get(ym).add(price));
                        }
                    }
                }
            }
        }

        List<PhotographerAnalyticsDto.MonthlyTrendDto> monthlyTrends = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
        for (YearMonth ym : countMap.keySet()) {
            monthlyTrends.add(new PhotographerAnalyticsDto.MonthlyTrendDto(
                    ym.format(formatter),
                    countMap.get(ym),
                    grossMap.get(ym),
                    completedMap.get(ym)
            ));
        }
        dto.setMonthlyTrends(monthlyTrends);

        // 7. Recent notifications
        if (notificationRepository != null) {
            List<Notification> notifs = notificationRepository.findTop20ByRecipientIdOrderByCreatedAtDesc(photographerUserId);
            List<NotificationDto> notifDtos = notifs.stream()
                    .limit(5)
                    .map(NotificationDto::fromEntity)
                    .toList();
            dto.setRecentNotifications(notifDtos);
        } else {
            dto.setRecentNotifications(Collections.emptyList());
        }

        return dto;
    }

    private PhotographerProfile requireActivePhotographer(Long userId) {
        if (userId == null) {
            throw new UnauthorizedException("A signed-in photographer is required.");
        }
        PhotographerProfile profile = photographerProfileRepository.findByUserIdWithUser(userId)
                .orElseThrow(() -> new PhotographerStudioProfileNotFoundException(
                        "Photographer profile not found."));
        if (profile.getUser().getRole() != UserRole.PHOTOGRAPHER
                || profile.getUser().getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Only active photographers can access Studio.");
        }
        if (profile.getVerificationStatus() == PhotographerVerificationStatus.SUSPENDED
                || profile.getVerificationStatus() == PhotographerVerificationStatus.REJECTED) {
            throw new PhotographerStudioProfileNotFoundException("Only approved photographers can access Studio.");
        }
        return profile;
    }

    private Map<Long, Deposit> loadDeposits(List<Booking> bookings) {
        if (bookings.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> ids = bookings.stream().map(Booking::getId).toList();
        Map<Long, Deposit> result = new LinkedHashMap<>();
        for (Deposit deposit : depositRepository.findByBookingIdIn(ids)) {
            result.put(deposit.getBooking().getId(), deposit);
        }
        return result;
    }

    private List<PhotographerDashboardView.AttentionItem> buildAttentionItems(
            List<Booking> pendingRequests, long pendingRequestCount,
            List<Booking> upcomingBookings, int completeness,
            long portfolioCount, boolean hasCover, LocalDate today) {
        List<PhotographerDashboardView.AttentionItem> items = new ArrayList<>();

        for (Booking booking : pendingRequests) {
            items.add(new PhotographerDashboardView.AttentionItem(
                    "BOOKING_REQUEST",
                    "Booking request from " + booking.getCustomer().getFullName(),
                    booking.getBookingDate() + " at " + booking.getBookingTime()
                            + " · " + booking.getLocation(),
                    action("Review Request", "/photographer/bookings/" + booking.getId(),
                            "primary", "GET"),
                    10));
        }
        if (pendingRequestCount > pendingRequests.size()) {
            items.add(new PhotographerDashboardView.AttentionItem(
                    "BOOKING_REQUESTS",
                    (pendingRequestCount - pendingRequests.size()) + " more requests await a response",
                    "Open all booking requests to review the remaining inquiries.",
                    action("View All Requests", "/photographer/bookings", "primary", "GET"),
                    11));
        }

        upcomingBookings.stream()
                .filter(booking -> !booking.getBookingDate().isAfter(today.plusDays(IMMINENT_SHOOT_DAYS)))
                .findFirst()
                .ifPresent(booking -> items.add(new PhotographerDashboardView.AttentionItem(
                        "UPCOMING_SHOOT",
                        "Upcoming shoot with " + booking.getCustomer().getFullName(),
                        booking.getBookingDate() + " at " + booking.getBookingTime()
                                + " · " + booking.getLocation(),
                        action("Open Chat", "/bookings/" + booking.getId() + "/chat",
                                "secondary", "GET"),
                        20)));

        if (completeness < 100) {
            items.add(new PhotographerDashboardView.AttentionItem(
                    "PROFILE_INCOMPLETE",
                    "Complete your professional profile",
                    "Your profile is " + completeness
                            + "% complete. Add the missing details clients rely on.",
                    action("Complete Profile", "/photographer/profile/edit", "secondary", "GET"),
                    30));
        }
        if (portfolioCount == 0) {
            items.add(new PhotographerDashboardView.AttentionItem(
                    "PORTFOLIO_EMPTY",
                    "Add work to your portfolio",
                    "Let clients evaluate your style with real portfolio photographs.",
                    action("Manage Portfolio", "/photographer/portfolio", "secondary", "GET"),
                    40));
        } else if (!hasCover) {
            items.add(new PhotographerDashboardView.AttentionItem(
                    "PORTFOLIO_COVER_MISSING",
                    "Choose a portfolio cover",
                    "Select a lead photograph for your public profile.",
                    action("Manage Portfolio", "/photographer/portfolio", "secondary", "GET"),
                    41));
        }

        return items.stream()
                .sorted(Comparator.comparingInt(PhotographerDashboardView.AttentionItem::getPriority))
                .limit(ATTENTION_LIMIT)
                .toList();
    }

    private PhotographerDashboardView.BookingRequestCard toRequestCard(Booking booking) {
        String basePath = "/photographer/bookings/" + booking.getId();
        return new PhotographerDashboardView.BookingRequestCard(
                booking.getId(),
                booking.getCustomer().getFullName(),
                booking.getBookingDate(),
                booking.getBookingTime(),
                booking.getLocation(),
                summarize(booking.getNotes()),
                booking.getAgreedPrice(),
                booking.getStatus(),
                List.of(
                        action("Review", basePath, "secondary", "GET"),
                        action("Accept", basePath + "/accept", "primary", "POST"),
                        action("Reject", basePath + "/reject", "danger", "POST")
                ));
    }

    private PhotographerDashboardView.UpcomingShootCard toUpcomingShootCard(
            Booking booking, Deposit deposit) {
        DepositStatus status = deposit != null ? deposit.getStatus() : null;
        String basePath = "/photographer/bookings/" + booking.getId();
        return new PhotographerDashboardView.UpcomingShootCard(
                booking.getId(),
                booking.getCustomer().getFullName(),
                booking.getBookingDate(),
                booking.getBookingTime(),
                booking.getLocation(),
                booking.getStatus(),
                status,
                depositLabel(status),
                List.of(
                        action("View Booking", basePath, "secondary", "GET"),
                        action("Open Chat", "/bookings/" + booking.getId() + "/chat", "text", "GET")
                ));
    }

    private PhotographerDashboardView.PortfolioPreview toPortfolioPreview(
            PortfolioImage image, String professionalName) {
        String category = image.getCategoryDisplayName();
        String altText = professionalName + " portfolio photograph · " + category;
        return new PhotographerDashboardView.PortfolioPreview(
                image.getThumbnailUrl(), image.getCaption(), category, altText);
    }

    private String professionalName(PhotographerProfile profile) {
        if (profile.getDisplayName() != null && !profile.getDisplayName().isBlank()) {
            return profile.getDisplayName();
        }
        return profile.getUser().getFullName();
    }

    private String summarize(String notes) {
        if (notes == null || notes.isBlank()) {
            return null;
        }
        String normalized = notes.trim().replaceAll("\\s+", " ");
        return normalized.length() <= 180 ? normalized : normalized.substring(0, 177) + "...";
    }

    private String depositLabel(DepositStatus status) {
        if (status == null || status == DepositStatus.PENDING) {
            return "Awaiting Customer Deposit";
        }
        return switch (status) {
            case PROCESSING -> "Customer Deposit Processing";
            case PAID -> "Deposit Paid";
            case FAILED -> "Customer Deposit Failed";
            case CANCELLED -> "Customer Deposit Cancelled";
            case REFUNDED -> "Deposit Refunded";
            case FORFEITED -> "Deposit Forfeited";
            case PENDING -> "Awaiting Customer Deposit";
        };
    }

    private PhotographerDashboardView.Action action(
            String label, String path, String style, String method) {
        return new PhotographerDashboardView.Action(label, path, style, method);
    }
}
