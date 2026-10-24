package com.photoconnect.entity;

public enum NotificationType {
    BOOKING_REQUEST("New Booking Request", "bi-calendar-plus", "pc-badge-primary"),
    BOOKING_ACCEPTED("Booking Accepted", "bi-calendar-check", "pc-badge-success"),
    BOOKING_REJECTED("Booking Declined", "bi-calendar-x", "pc-badge-danger"),
    DEPOSIT_CONFIRMED("Deposit Paid", "bi-credit-card-check", "pc-badge-success"),
    BOOKING_CANCELLED("Booking Cancelled", "bi-x-circle", "pc-badge-danger"),
    SCHEDULE_CHANGED("Schedule Changed", "bi-clock-history", "pc-badge-warning"),
    NEW_MESSAGE("New Message", "bi-chat-dots", "pc-badge-info"),
    DELIVERY_READY("Delivery Album Ready", "bi-images", "pc-badge-primary"),
    DELIVERY_CONFIRMED("Delivery Accepted", "bi-check2-all", "pc-badge-success"),
    NEW_REVIEW("New Client Review", "bi-star-fill", "pc-badge-warning");

    private final String displayName;
    private final String iconClass;
    private final String badgeClass;

    NotificationType(String displayName, String iconClass, String badgeClass) {
        this.displayName = displayName;
        this.iconClass = iconClass;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIconClass() {
        return iconClass;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
