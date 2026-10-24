package com.photoconnect.dto;

public class FeedCreatorDto {
    private final Long photographerId;
    private final String displayName;
    private final String avatarUrl;
    private final String city;
    private final Double averageRating;

    public FeedCreatorDto(Long photographerId, String displayName, String avatarUrl, String city, Double averageRating) {
        this.photographerId = photographerId;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.city = city;
        this.averageRating = averageRating;
    }

    public Long getPhotographerId() { return photographerId; }
    public String getDisplayName() { return displayName; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getCity() { return city; }
    public Double getAverageRating() { return averageRating; }
}
