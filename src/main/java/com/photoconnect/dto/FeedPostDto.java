package com.photoconnect.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FeedPostDto {
    private final Long imageId;
    private final String imageUrl;
    private final String caption;
    private final String category;
    private final String categoryDisplayName;
    private final LocalDateTime createdAt;
    private final String timeAgo;
    private final Long photographerId;
    private final String photographerName;
    private final String photographerCity;
    private final BigDecimal startingPrice;
    private final Double averageRating;
    private final Integer reviewCount;
    private final int likeCount;
    private final boolean isSaved;
    private final String photographerAvatarUrl;
    private final int commentCount;

    public FeedPostDto(Long imageId, String imageUrl, String caption, String category,
                       String categoryDisplayName, LocalDateTime createdAt, String timeAgo,
                       Long photographerId, String photographerName, String photographerAvatarUrl,
                       String photographerCity, BigDecimal startingPrice, Double averageRating,
                       Integer reviewCount, int likeCount, boolean isSaved, int commentCount) {
        this.imageId = imageId;
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.category = category;
        this.categoryDisplayName = categoryDisplayName;
        this.createdAt = createdAt;
        this.timeAgo = timeAgo;
        this.photographerId = photographerId;
        this.photographerName = photographerName;
        this.photographerAvatarUrl = photographerAvatarUrl;
        this.photographerCity = photographerCity;
        this.startingPrice = startingPrice;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
        this.likeCount = likeCount;
        this.isSaved = isSaved;
        this.commentCount = commentCount;
    }

    public Long getImageId() { return imageId; }
    public String getPhotographerAvatarUrl() { return photographerAvatarUrl; }
    public String getImageUrl() { return imageUrl; }
    public String getCaption() { return caption; }
    public String getCategory() { return category; }
    public String getCategoryDisplayName() { return categoryDisplayName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getTimeAgo() { return timeAgo; }
    public Long getPhotographerId() { return photographerId; }
    public String getPhotographerName() { return photographerName; }
    public String getPhotographerCity() { return photographerCity; }
    public BigDecimal getStartingPrice() { return startingPrice; }
    public Double getAverageRating() { return averageRating; }
    public Integer getReviewCount() { return reviewCount; }
    public int getLikeCount() { return likeCount; }
    public boolean isSaved() { return isSaved; }
    public boolean getIsSaved() { return isSaved; }
    public int getCommentCount() { return commentCount; }
}
