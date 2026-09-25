package com.photoconnect.service;

import com.photoconnect.dto.PhotographerDashboardView;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.entity.Deposit;
import com.photoconnect.entity.DepositStatus;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerUnavailableDate;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.PortfolioImage;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.exception.UnauthorizedException;
import com.photoconnect.exception.PhotographerStudioProfileNotFoundException;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.DepositRepository;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.repository.PhotographerUnavailableDateRepository;
import com.photoconnect.repository.PortfolioImageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    public PhotographerDashboardServiceImpl(
            PhotographerProfileRepository photographerProfileRepository,
            BookingRepository bookingRepository,
            DepositRepository depositRepository,
            PortfolioImageRepository portfolioImageRepository,
            PhotographerUnavailableDateRepository unavailableDateRepository,
            PhotographerProfileService photographerProfileService) {
        this.photographerProfileRepository = photographerProfileRepository;
        this.bookingRepository = bookingRepository;
        this.depositRepository = depositRepository;
        this.portfolioImageRepository = portfolioImageRepository;
        this.unavailableDateRepository = unavailableDateRepository;
        this.photographerProfileService = photographerProfileService;
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
