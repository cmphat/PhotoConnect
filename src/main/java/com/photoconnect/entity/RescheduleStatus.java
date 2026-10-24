package com.photoconnect.entity;

public enum RescheduleStatus {
    PENDING("Pending Confirmation", "pc-badge-warning"),
    ACCEPTED("Accepted", "pc-badge-success"),
    REJECTED("Declined", "pc-badge-danger"),
    CANCELLED("Cancelled", "pc-badge-secondary");

    private final String displayName;
    private final String badgeClass;

    RescheduleStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
