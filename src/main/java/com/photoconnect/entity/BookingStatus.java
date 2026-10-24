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

    public static final java.util.Set<BookingStatus> TIME_RESERVING_STATUSES = java.util.Set.of(
            ACCEPTED,
            DEPOSIT_PENDING,
            CONFIRMED,
            SHOOTING,
            POST_PROCESSING,
            DELIVERED,
            COMPLETED
    );

    public static final java.util.List<BookingStatus> TIME_RESERVING_STATUSES_LIST = java.util.List.of(
            ACCEPTED,
            DEPOSIT_PENDING,
            CONFIRMED,
            SHOOTING,
            POST_PROCESSING,
            DELIVERED,
            COMPLETED
    );

    public static java.util.Set<BookingStatus> getTimeReservingStatuses() {
        return TIME_RESERVING_STATUSES;
    }

    public static java.util.List<BookingStatus> getTimeReservingStatusesList() {
        return TIME_RESERVING_STATUSES_LIST;
    }

    public boolean isTimeReserving() {
        return TIME_RESERVING_STATUSES.contains(this);
    }
}
