package com.photoconnect.dto;

import com.photoconnect.entity.BookingStatus;
import com.photoconnect.entity.DepositStatus;
import com.photoconnect.entity.PhotographerVerificationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Immutable, view-safe model for the authenticated photographer Studio. */
public class PhotographerDashboardView {

    private final Long profileId;
    private final String professionalName;
    private final PhotographerVerificationStatus verificationStatus;
    private final boolean publicProfileAvailable;
    private final int profileCompleteness;
    private final List<AttentionItem> attentionItems;
    private final List<BookingRequestCard> bookingRequests;
    private final List<UpcomingShootCard> upcomingShoots;
    private final PortfolioSummary portfolioSummary;
    private final List<PortfolioPreview> portfolioPreview;
    private final AvailabilitySummary availabilitySummary;
    private final RatingSummary ratingSummary;

    public PhotographerDashboardView(Long profileId, String professionalName,
                                     PhotographerVerificationStatus verificationStatus,
                                     boolean publicProfileAvailable, int profileCompleteness,
                                     List<AttentionItem> attentionItems,
                                     List<BookingRequestCard> bookingRequests,
                                     List<UpcomingShootCard> upcomingShoots,
                                     PortfolioSummary portfolioSummary,
                                     List<PortfolioPreview> portfolioPreview,
                                     AvailabilitySummary availabilitySummary,
                                     RatingSummary ratingSummary) {
        this.profileId = profileId;
        this.professionalName = professionalName;
        this.verificationStatus = verificationStatus;
        this.publicProfileAvailable = publicProfileAvailable;
        this.profileCompleteness = profileCompleteness;
        this.attentionItems = List.copyOf(attentionItems);
        this.bookingRequests = List.copyOf(bookingRequests);
        this.upcomingShoots = List.copyOf(upcomingShoots);
        this.portfolioSummary = portfolioSummary;
        this.portfolioPreview = List.copyOf(portfolioPreview);
        this.availabilitySummary = availabilitySummary;
        this.ratingSummary = ratingSummary;
    }

    public Long getProfileId() { return profileId; }
    public String getProfessionalName() { return professionalName; }
    public PhotographerVerificationStatus getVerificationStatus() { return verificationStatus; }
    public boolean isPublicProfileAvailable() { return publicProfileAvailable; }
    public boolean getPublicProfileAvailable() { return publicProfileAvailable; }
    public int getProfileCompleteness() { return profileCompleteness; }
    public List<AttentionItem> getAttentionItems() { return attentionItems; }
    public List<BookingRequestCard> getBookingRequests() { return bookingRequests; }
    public List<UpcomingShootCard> getUpcomingShoots() { return upcomingShoots; }
    public PortfolioSummary getPortfolioSummary() { return portfolioSummary; }
    public List<PortfolioPreview> getPortfolioPreview() { return portfolioPreview; }
    public AvailabilitySummary getAvailabilitySummary() { return availabilitySummary; }
    public RatingSummary getRatingSummary() { return ratingSummary; }

    public static class Action {
        private final String label;
        private final String path;
        private final String style;
        private final String method;

        public Action(String label, String path, String style, String method) {
            this.label = label;
            this.path = path;
            this.style = style;
            this.method = method;
        }

        public String getLabel() { return label; }
        public String getPath() { return path; }
        public String getStyle() { return style; }
        public String getMethod() { return method; }
    }

    public static class AttentionItem {
        private final String type;
        private final String title;
        private final String description;
        private final Action action;
        private final int priority;

        public AttentionItem(String type, String title, String description,
                             Action action, int priority) {
            this.type = type;
            this.title = title;
            this.description = description;
            this.action = action;
            this.priority = priority;
        }

        public String getType() { return type; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public Action getAction() { return action; }
        public int getPriority() { return priority; }
    }

    public static class BookingRequestCard {
        private final Long id;
        private final String customerName;
        private final LocalDate bookingDate;
        private final LocalTime bookingTime;
        private final String location;
        private final String context;
        private final BigDecimal agreedPrice;
        private final BookingStatus status;
        private final List<Action> actions;

        public BookingRequestCard(Long id, String customerName, LocalDate bookingDate,
                                  LocalTime bookingTime, String location, String context,
                                  BigDecimal agreedPrice, BookingStatus status,
                                  List<Action> actions) {
            this.id = id;
            this.customerName = customerName;
            this.bookingDate = bookingDate;
            this.bookingTime = bookingTime;
            this.location = location;
            this.context = context;
            this.agreedPrice = agreedPrice;
            this.status = status;
            this.actions = List.copyOf(actions);
        }

        public Long getId() { return id; }
        public String getCustomerName() { return customerName; }
        public LocalDate getBookingDate() { return bookingDate; }
        public LocalTime getBookingTime() { return bookingTime; }
        public String getLocation() { return location; }
        public String getContext() { return context; }
        public BigDecimal getAgreedPrice() { return agreedPrice; }
        public BookingStatus getStatus() { return status; }
        public List<Action> getActions() { return actions; }
    }

    public static class UpcomingShootCard {
        private final Long id;
        private final String customerName;
        private final LocalDate bookingDate;
        private final LocalTime bookingTime;
        private final String location;
        private final BookingStatus status;
        private final DepositStatus depositStatus;
        private final String depositLabel;
        private final List<Action> actions;

        public UpcomingShootCard(Long id, String customerName, LocalDate bookingDate,
                                 LocalTime bookingTime, String location, BookingStatus status,
                                 DepositStatus depositStatus, String depositLabel,
                                 List<Action> actions) {
            this.id = id;
            this.customerName = customerName;
            this.bookingDate = bookingDate;
            this.bookingTime = bookingTime;
            this.location = location;
            this.status = status;
            this.depositStatus = depositStatus;
            this.depositLabel = depositLabel;
            this.actions = List.copyOf(actions);
        }

        public Long getId() { return id; }
        public String getCustomerName() { return customerName; }
        public LocalDate getBookingDate() { return bookingDate; }
        public LocalTime getBookingTime() { return bookingTime; }
        public String getLocation() { return location; }
        public BookingStatus getStatus() { return status; }
        public DepositStatus getDepositStatus() { return depositStatus; }
        public String getDepositLabel() { return depositLabel; }
        public List<Action> getActions() { return actions; }
    }

    public static class PortfolioSummary {
        private final long imageCount;
        private final long categoryCount;
        private final boolean hasCover;

        public PortfolioSummary(long imageCount, long categoryCount, boolean hasCover) {
            this.imageCount = imageCount;
            this.categoryCount = categoryCount;
            this.hasCover = hasCover;
        }

        public long getImageCount() { return imageCount; }
        public long getCategoryCount() { return categoryCount; }
        public boolean isHasCover() { return hasCover; }
        public boolean getHasCover() { return hasCover; }
    }

    public static class PortfolioPreview {
        private final String thumbnailUrl;
        private final String caption;
        private final String category;
        private final String altText;

        public PortfolioPreview(String thumbnailUrl, String caption, String category, String altText) {
            this.thumbnailUrl = thumbnailUrl;
            this.caption = caption;
            this.category = category;
            this.altText = altText;
        }

        public String getThumbnailUrl() { return thumbnailUrl; }
        public String getCaption() { return caption; }
        public String getCategory() { return category; }
        public String getAltText() { return altText; }
    }

    public static class AvailabilitySummary {
        private final long upcomingBlockedCount;
        private final LocalDate nextUnavailableDate;

        public AvailabilitySummary(long upcomingBlockedCount, LocalDate nextUnavailableDate) {
            this.upcomingBlockedCount = upcomingBlockedCount;
            this.nextUnavailableDate = nextUnavailableDate;
        }

        public long getUpcomingBlockedCount() { return upcomingBlockedCount; }
        public LocalDate getNextUnavailableDate() { return nextUnavailableDate; }
    }

    public static class RatingSummary {
        private final double averageRating;
        private final int reviewCount;

        public RatingSummary(double averageRating, int reviewCount) {
            this.averageRating = averageRating;
            this.reviewCount = reviewCount;
        }

        public double getAverageRating() { return averageRating; }
        public int getReviewCount() { return reviewCount; }
    }
}
