package com.photoconnect.entity;

/**
 * Lifecycle status of a customer booking with a photographer across the full PhotoConnect V4 state machine.
 */
public enum BookingStatus {
    PENDING("Requested", "pc-badge-warning"),
    ACCEPTED("Accepted", "pc-badge-info"),
    DEPOSIT_PENDING("Deposit Pending", "pc-badge-warning"),
    CONFIRMED("Confirmed", "pc-badge-primary"),
    SHOOTING("Shooting", "pc-badge-primary"),
    POST_PROCESSING("Post Processing", "pc-badge-info"),
    DELIVERED("Delivered", "pc-badge-success"),
    COMPLETED("Completed", "pc-badge-midnight"),
    REJECTED("Declined", "pc-badge-danger"),
    CANCELLED("Cancelled", "pc-badge-secondary"),
    EXPIRED("Expired", "pc-badge-secondary"),
    DISPUTED("Disputed", "pc-badge-danger");

    private final String displayName;
    private final String badgeClass;

    BookingStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED || this == REJECTED || this == EXPIRED;
    }

    public boolean isTimeReserving() {
        return this == ACCEPTED || this == DEPOSIT_PENDING || this == CONFIRMED ||
               this == SHOOTING || this == POST_PROCESSING || this == DELIVERED || this == COMPLETED;
    }
}
