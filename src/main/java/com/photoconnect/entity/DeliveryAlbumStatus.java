package com.photoconnect.entity;

/**
 * Status lifecycle of a customer delivery album for finished photography deliverables.
 */
public enum DeliveryAlbumStatus {
    DRAFT("Draft / In Preparation", "pc-badge-warning"),
    DELIVERED("Delivered / Awaiting Review", "pc-badge-info"),
    ACCEPTED("Accepted", "pc-badge-success"),
    REVISION_REQUESTED("Revision Requested", "pc-badge-danger");

    private final String displayName;
    private final String badgeClass;

    DeliveryAlbumStatus(String displayName, String badgeClass) {
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
