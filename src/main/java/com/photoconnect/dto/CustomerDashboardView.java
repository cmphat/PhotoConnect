package com.photoconnect.dto;

import com.photoconnect.entity.BookingStatus;
import com.photoconnect.entity.DepositStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** Immutable, view-safe model for the customer workspace. */
public class CustomerDashboardView {

    private final String customerName;
    private final Summary summary;
    private final List<AttentionItem> attentionItems;
    private final List<BookingCard> bookings;
    private final List<SavedPhotographerCard> savedPhotographers;

    public CustomerDashboardView(String customerName, Summary summary,
                                 List<AttentionItem> attentionItems,
                                 List<BookingCard> bookings,
                                 List<SavedPhotographerCard> savedPhotographers) {
        this.customerName = customerName;
        this.summary = summary;
        this.attentionItems = List.copyOf(attentionItems);
        this.bookings = List.copyOf(bookings);
        this.savedPhotographers = List.copyOf(savedPhotographers);
    }

    public String getCustomerName() { return customerName; }
    public Summary getSummary() { return summary; }
    public List<AttentionItem> getAttentionItems() { return attentionItems; }
    public List<BookingCard> getBookings() { return bookings; }
    public List<SavedPhotographerCard> getSavedPhotographers() { return savedPhotographers; }

    public static class Summary {
        private final long upcomingCount;
        private final long pendingCount;
        private final long completedCount;
        private final long savedCount;

        public Summary(long upcomingCount, long pendingCount, long completedCount, long savedCount) {
            this.upcomingCount = upcomingCount;
            this.pendingCount = pendingCount;
            this.completedCount = completedCount;
            this.savedCount = savedCount;
        }

        public long getUpcomingCount() { return upcomingCount; }
        public long getPendingCount() { return pendingCount; }
        public long getCompletedCount() { return completedCount; }
        public long getSavedCount() { return savedCount; }
    }

    public static class Action {
        private final String label;
        private final String path;
        private final String style;

        public Action(String label, String path, String style) {
            this.label = label;
            this.path = path;
            this.style = style;
        }

        public String getLabel() { return label; }
        public String getPath() { return path; }
        public String getStyle() { return style; }
    }

    public static class AttentionItem {
        private final Long bookingId;
        private final int priority;
        private final String category;
        private final String title;
        private final String description;
        private final BigDecimal amount;
        private final Action action;

        public AttentionItem(Long bookingId, int priority, String category, String title, String description,
                             BigDecimal amount, Action action) {
            this.bookingId = bookingId;
            this.priority = priority;
            this.category = category;
            this.title = title;
            this.description = description;
            this.amount = amount;
            this.action = action;
        }

        public Long getBookingId() { return bookingId; }
        public int getPriority() { return priority; }
        public String getCategory() { return category; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public BigDecimal getAmount() { return amount; }
        public Action getAction() { return action; }
    }

    public static class BookingCard {
        private final Long id;
        private final Long photographerId;
        private final String photographerName;
        private final String coverImageUrl;
        private final LocalDate bookingDate;
        private final LocalTime bookingTime;
        private final String location;
        private final BookingStatus status;
        private final BigDecimal agreedPrice;
        private final BigDecimal depositAmount;
        private final DepositStatus depositStatus;
        private final String depositLabel;
        private final List<Action> actions;

        public BookingCard(Long id, Long photographerId, String photographerName, String coverImageUrl,
                           LocalDate bookingDate, LocalTime bookingTime, String location,
                           BookingStatus status, BigDecimal agreedPrice, BigDecimal depositAmount,
                           DepositStatus depositStatus, String depositLabel, List<Action> actions) {
            this.id = id;
            this.photographerId = photographerId;
            this.photographerName = photographerName;
            this.coverImageUrl = coverImageUrl;
            this.bookingDate = bookingDate;
            this.bookingTime = bookingTime;
            this.location = location;
            this.status = status;
            this.agreedPrice = agreedPrice;
            this.depositAmount = depositAmount;
            this.depositStatus = depositStatus;
            this.depositLabel = depositLabel;
            this.actions = List.copyOf(actions);
        }

        public Long getId() { return id; }
        public Long getPhotographerId() { return photographerId; }
        public String getPhotographerName() { return photographerName; }
        public String getCoverImageUrl() { return coverImageUrl; }
        public LocalDate getBookingDate() { return bookingDate; }
        public LocalTime getBookingTime() { return bookingTime; }
        public String getLocation() { return location; }
        public BookingStatus getStatus() { return status; }
        public BigDecimal getAgreedPrice() { return agreedPrice; }
        public BigDecimal getDepositAmount() { return depositAmount; }
        public DepositStatus getDepositStatus() { return depositStatus; }
        public String getDepositLabel() { return depositLabel; }
        public List<Action> getActions() { return actions; }
    }

    public static class SavedPhotographerCard {
        private final Long photographerId;
        private final String displayName;
        private final String headline;
        private final String location;
        private final String coverImageUrl;
        private final Double averageRating;
        private final BigDecimal priceFrom;
        private final LocalDateTime savedAt;

        public SavedPhotographerCard(Long photographerId, String displayName, String headline,
                                     String location, String coverImageUrl, Double averageRating,
                                     BigDecimal priceFrom, LocalDateTime savedAt) {
            this.photographerId = photographerId;
            this.displayName = displayName;
            this.headline = headline;
            this.location = location;
            this.coverImageUrl = coverImageUrl;
            this.averageRating = averageRating;
            this.priceFrom = priceFrom;
            this.savedAt = savedAt;
        }

        public Long getPhotographerId() { return photographerId; }
        public String getDisplayName() { return displayName; }
        public String getHeadline() { return headline; }
        public String getLocation() { return location; }
        public String getCoverImageUrl() { return coverImageUrl; }
        public Double getAverageRating() { return averageRating; }
        public BigDecimal getPriceFrom() { return priceFrom; }
        public LocalDateTime getSavedAt() { return savedAt; }
    }
}
