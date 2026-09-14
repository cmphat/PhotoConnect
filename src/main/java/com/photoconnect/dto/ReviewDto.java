package com.photoconnect.dto;

import com.photoconnect.entity.Review;
import com.photoconnect.entity.ReviewStatus;

import java.time.LocalDateTime;

/**
 * View-safe DTO for customer reviews.
 */
public class ReviewDto {

    private final Long id;
    private final Long bookingId;
    private final Long photographerProfileId;
    private final Integer rating;
    private final String comment;
    private final String customerName;
    private final ReviewStatus status;
    private final LocalDateTime createdAt;

    public ReviewDto(Long id, Long bookingId, Long photographerProfileId, Integer rating, String comment, String customerName, LocalDateTime createdAt) {
        this(id, bookingId, photographerProfileId, rating, comment, customerName, ReviewStatus.VISIBLE, createdAt);
    }

    public ReviewDto(Long id, Long bookingId, Long photographerProfileId, Integer rating, String comment, String customerName, ReviewStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.photographerProfileId = photographerProfileId;
        this.rating = rating;
        this.comment = comment;
        this.customerName = customerName;
        this.status = status != null ? status : ReviewStatus.VISIBLE;
        this.createdAt = createdAt;
    }

    public static ReviewDto from(Review review) {
        if (review == null) {
            return null;
        }
        String customerName = "Verified Customer";
        if (review.getCustomer() != null && review.getCustomer().getFullName() != null && !review.getCustomer().getFullName().isBlank()) {
            customerName = review.getCustomer().getFullName();
        }
        return new ReviewDto(
                review.getId(),
                review.getBooking() != null ? review.getBooking().getId() : null,
                review.getPhotographerProfile() != null ? review.getPhotographerProfile().getId() : null,
                review.getRating(),
                review.getComment(),
                customerName,
                review.getStatus() != null ? review.getStatus() : ReviewStatus.VISIBLE,
                review.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getPhotographerProfileId() {
        return photographerProfileId;
    }

    public Integer getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getCustomerName() {
        return customerName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    public boolean isHidden() {
        return ReviewStatus.HIDDEN.equals(status);
    }
}
