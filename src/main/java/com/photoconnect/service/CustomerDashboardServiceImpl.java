package com.photoconnect.service;

import com.photoconnect.dto.CustomerDashboardView;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.entity.Deposit;
import com.photoconnect.entity.DepositStatus;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.PortfolioImage;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;

@Service
@Transactional(readOnly = true)
public class CustomerDashboardServiceImpl implements CustomerDashboardService {

    private static final int BOOKING_PREVIEW_LIMIT = 5;
    private static final int SAVED_PREVIEW_LIMIT = 4;
    private static final int ATTENTION_LIMIT = 4;
    private static final List<BookingStatus> ACTIVE_BOOKING_STATUSES =
            List.of(BookingStatus.PENDING, BookingStatus.ACCEPTED);

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final DepositRepository depositRepository;
    private final ReviewRepository reviewRepository;
    private final SavedPhotographerRepository savedPhotographerRepository;
    private final PortfolioImageRepository portfolioImageRepository;
    private final DepositService depositService;

    public CustomerDashboardServiceImpl(UserRepository userRepository,
                                        BookingRepository bookingRepository,
                                        DepositRepository depositRepository,
                                        ReviewRepository reviewRepository,
                                        SavedPhotographerRepository savedPhotographerRepository,
                                        PortfolioImageRepository portfolioImageRepository,
                                        DepositService depositService) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.depositRepository = depositRepository;
        this.reviewRepository = reviewRepository;
        this.savedPhotographerRepository = savedPhotographerRepository;
        this.portfolioImageRepository = portfolioImageRepository;
        this.depositService = depositService;
    }

    @Override
    public CustomerDashboardView getDashboard(Long customerUserId) {
        User customer = requireActiveCustomer(customerUserId);
        LocalDate today = LocalDate.now();

        CustomerDashboardView.Summary summary = new CustomerDashboardView.Summary(
                bookingRepository.countUpcomingForCustomer(customerUserId, today, ACTIVE_BOOKING_STATUSES),
                bookingRepository.countByCustomerIdAndStatus(customerUserId, BookingStatus.PENDING),
                bookingRepository.countByCustomerIdAndStatus(customerUserId, BookingStatus.COMPLETED),
                savedPhotographerRepository.countApprovedByCustomerId(customerUserId)
        );

        List<Booking> bookingPreviews = bookingRepository.findDashboardBookings(
                customerUserId, today, ACTIVE_BOOKING_STATUSES, PageRequest.of(0, BOOKING_PREVIEW_LIMIT));
        List<Booking> attentionCandidates =
                bookingRepository.findDashboardAttentionCandidates(customerUserId);
        List<SavedPhotographer> savedPreviews = savedPhotographerRepository.findApprovedPreviewByCustomerId(
                        customerUserId, PageRequest.of(0, SAVED_PREVIEW_LIMIT)).stream()
                .filter(saved -> saved.getPhotographerProfile().getVerificationStatus()
                        == PhotographerVerificationStatus.APPROVED)
                .toList();

        Set<Long> bookingIds = new LinkedHashSet<>();
        bookingPreviews.forEach(booking -> bookingIds.add(booking.getId()));
        attentionCandidates.forEach(booking -> bookingIds.add(booking.getId()));

        Map<Long, Deposit> deposits = loadDeposits(bookingIds);
        Set<Long> reviewedBookingIds = loadReviewedBookingIds(bookingIds);
        Map<Long, String> coverImages = loadCoverImages(bookingPreviews, savedPreviews);

        List<CustomerDashboardView.AttentionItem> attentionItems = buildAttentionItems(
                attentionCandidates, deposits, reviewedBookingIds, today);
        List<CustomerDashboardView.BookingCard> bookingCards = bookingPreviews.stream()
                .map(booking -> toBookingCard(
                        booking,
                        deposits.get(booking.getId()),
                        reviewedBookingIds.contains(booking.getId()),
                        coverImages.get(booking.getPhotographerProfile().getId())))
                .toList();
        List<CustomerDashboardView.SavedPhotographerCard> savedCards = savedPreviews.stream()
                .map(saved -> toSavedCard(
                        saved,
                        coverImages.get(saved.getPhotographerProfile().getId())))
                .toList();

        return new CustomerDashboardView(
                customer.getFullName(), summary, attentionItems, bookingCards, savedCards);
    }

    private User requireActiveCustomer(Long customerUserId) {
        if (customerUserId == null) {
            throw new UnauthorizedException("A signed-in customer is required.");
        }
        User user = userRepository.findById(customerUserId)
                .orElseThrow(() -> new UnauthorizedException("Customer account not found."));
        if (user.getRole() != UserRole.CUSTOMER || user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Only active customers can access this workspace.");
        }
        return user;
    }

    private Map<Long, Deposit> loadDeposits(Set<Long> bookingIds) {
        if (bookingIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Deposit> result = new LinkedHashMap<>();
        for (Deposit deposit : depositRepository.findByBookingIdIn(List.copyOf(bookingIds))) {
            result.put(deposit.getBooking().getId(), deposit);
        }
        return result;
    }

    private Set<Long> loadReviewedBookingIds(Set<Long> bookingIds) {
        if (bookingIds.isEmpty()) {
            return Collections.emptySet();
        }
        return new LinkedHashSet<>(reviewRepository.findReviewedBookingIds(List.copyOf(bookingIds)));
    }

    private Map<Long, String> loadCoverImages(List<Booking> bookings,
                                               List<SavedPhotographer> savedPhotographers) {
        Set<Long> profileIds = new LinkedHashSet<>();
        bookings.forEach(booking -> profileIds.add(booking.getPhotographerProfile().getId()));
        savedPhotographers.forEach(saved -> profileIds.add(saved.getPhotographerProfile().getId()));
        if (profileIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, String> covers = new LinkedHashMap<>();
        for (PortfolioImage image : portfolioImageRepository.findDashboardImageCandidates(List.copyOf(profileIds))) {
            Long profileId = image.getPhotographerProfile().getId();
            covers.putIfAbsent(profileId, image.getCoverTransformedUrl());
        }
        return covers;
    }

    private List<CustomerDashboardView.AttentionItem> buildAttentionItems(
            List<Booking> candidates, Map<Long, Deposit> deposits,
            Set<Long> reviewedBookingIds, LocalDate today) {
        List<CustomerDashboardView.AttentionItem> items = new ArrayList<>();

        for (Booking booking : candidates) {
            Deposit deposit = deposits.get(booking.getId());
            if (booking.getStatus() == BookingStatus.ACCEPTED) {
                CustomerDashboardView.AttentionItem paymentItem = paymentAttention(booking, deposit);
                if (paymentItem != null) {
                    items.add(paymentItem);
                } else if (!booking.getBookingDate().isBefore(today)) {
                    items.add(new CustomerDashboardView.AttentionItem(
                            booking.getId(),
                            30,
                            "Upcoming session",
                            "Your shoot with " + photographerName(booking) + " is confirmed",
                            "Scheduled for " + booking.getBookingDate()
                                    + ". Use the booking chat to coordinate final details.",
                            null,
                            action("Open Chat", "/bookings/" + booking.getId() + "/chat", "primary")
                    ));
                }
            } else if (booking.getStatus() == BookingStatus.COMPLETED
                    && !reviewedBookingIds.contains(booking.getId())) {
                items.add(new CustomerDashboardView.AttentionItem(
                        booking.getId(),
                        40,
                        "Review available",
                        "How was your session with " + photographerName(booking) + "?",
                        "Share a review from this completed booking to help future customers.",
                        null,
                        action("Write Review", "/bookings/" + booking.getId() + "/review", "primary")
                ));
            }
        }

        return items.stream()
                .sorted(Comparator.comparingInt(CustomerDashboardView.AttentionItem::getPriority))
                .limit(ATTENTION_LIMIT)
                .toList();
    }

    private CustomerDashboardView.AttentionItem paymentAttention(Booking booking, Deposit deposit) {
        BigDecimal amount = deposit != null
                ? deposit.getAmount()
                : depositService.calculateDepositAmount(booking.getAgreedPrice());
        DepositStatus status = deposit != null ? deposit.getStatus() : null;
        String basePath = "/bookings/" + booking.getId();

        if (status == null || status == DepositStatus.PENDING) {
            return new CustomerDashboardView.AttentionItem(
                    booking.getId(),
                    10,
                    "Payment required",
                    "Secure your session with " + photographerName(booking),
                    "The photographer accepted your " + booking.getBookingDate()
                            + " session. Complete the 30% demo deposit to secure it.",
                    amount,
                    action("Pay Deposit", basePath + "/deposit/checkout", "primary"));
        }
        if (status == DepositStatus.FAILED || status == DepositStatus.CANCELLED) {
            return new CustomerDashboardView.AttentionItem(
                    booking.getId(),
                    20,
                    "Payment needs attention",
                    "Retry the deposit for " + photographerName(booking),
                    "The previous demo payment was not completed. Your existing checkout can be retried.",
                    amount,
                    action("Retry Payment", basePath + "/deposit/checkout", "primary"));
        }
        if (status == DepositStatus.PROCESSING) {
            return new CustomerDashboardView.AttentionItem(
                    booking.getId(),
                    25,
                    "Payment processing",
                    "Check the deposit status for " + photographerName(booking),
                    "A demo payment attempt is currently marked as processing.",
                    amount,
                    action("View Payment", basePath + "/deposit/result", "secondary"));
        }
        if (status == DepositStatus.REFUNDED || status == DepositStatus.FORFEITED) {
            return new CustomerDashboardView.AttentionItem(
                    booking.getId(),
                    50,
                    "Deposit status",
                    "Review this booking with " + photographerName(booking),
                    "The deposit has a final status that cannot be paid again from checkout.",
                    amount,
                    action("View Booking", basePath, "secondary"));
        }
        return null;
    }

    private CustomerDashboardView.BookingCard toBookingCard(Booking booking, Deposit deposit,
                                                              boolean reviewed, String coverImageUrl) {
        List<CustomerDashboardView.Action> actions = new ArrayList<>();
        String bookingPath = "/bookings/" + booking.getId();
        DepositStatus depositStatus = deposit != null ? deposit.getStatus() : null;
        BigDecimal depositAmount = deposit != null ? deposit.getAmount() : null;

        if (booking.getStatus() == BookingStatus.ACCEPTED) {
            if (depositStatus == DepositStatus.PAID) {
                actions.add(action("View Receipt", bookingPath + "/deposit/receipt", "secondary"));
            } else if (depositStatus == DepositStatus.PROCESSING) {
                actions.add(action("Payment Status", bookingPath + "/deposit/result", "primary"));
            } else if (depositStatus == null || depositStatus == DepositStatus.PENDING
                    || depositStatus == DepositStatus.FAILED || depositStatus == DepositStatus.CANCELLED) {
                depositAmount = depositAmount != null
                        ? depositAmount
                        : depositService.calculateDepositAmount(booking.getAgreedPrice());
                String label = depositStatus == DepositStatus.FAILED || depositStatus == DepositStatus.CANCELLED
                        ? "Retry Payment" : "Pay Deposit";
                actions.add(action(label, bookingPath + "/deposit/checkout", "primary"));
            }
        }
        if (booking.getStatus() == BookingStatus.COMPLETED && !reviewed) {
            actions.add(action("Write Review", bookingPath + "/review", "primary"));
        }
        actions.add(action("View Details", bookingPath, "secondary"));
        actions.add(action("Open Chat", bookingPath + "/chat", "text"));

        return new CustomerDashboardView.BookingCard(
                booking.getId(),
                booking.getPhotographerProfile().getId(),
                photographerName(booking),
                coverImageUrl,
                booking.getBookingDate(),
                booking.getBookingTime(),
                booking.getLocation(),
                booking.getStatus(),
                booking.getAgreedPrice(),
                depositAmount,
                depositStatus,
                depositLabel(booking, depositStatus),
                actions
        );
    }

    private String depositLabel(Booking booking, DepositStatus status) {
        if (status != null) {
            return switch (status) {
                case PENDING -> "Deposit pending";
                case PROCESSING -> "Payment processing";
                case PAID -> "Deposit paid";
                case FAILED -> "Payment failed";
                case CANCELLED -> "Payment cancelled";
                case REFUNDED -> "Deposit refunded";
                case FORFEITED -> "Deposit forfeited";
            };
        }
        return booking.getStatus() == BookingStatus.ACCEPTED ? "Deposit required" : "Not required yet";
    }

    private CustomerDashboardView.SavedPhotographerCard toSavedCard(
            SavedPhotographer saved, String coverImageUrl) {
        PhotographerProfile profile = saved.getPhotographerProfile();
        String location = joinLocation(profile.getCity(), profile.getCountry());
        return new CustomerDashboardView.SavedPhotographerCard(
                profile.getId(),
                profile.getDisplayName(),
                profile.getHeadline(),
                location,
                coverImageUrl,
                profile.getAverageRating() != null ? profile.getAverageRating() : 0.0,
                profile.getPriceFrom(),
                saved.getCreatedAt()
        );
    }

    private String photographerName(Booking booking) {
        return booking.getPhotographerProfile().getDisplayName();
    }

    private String joinLocation(String city, String country) {
        if (city == null || city.isBlank()) return country;
        if (country == null || country.isBlank()) return city;
        return city + ", " + country;
    }

    private CustomerDashboardView.Action action(String label, String path, String style) {
        return new CustomerDashboardView.Action(label, path, style);
    }
}
