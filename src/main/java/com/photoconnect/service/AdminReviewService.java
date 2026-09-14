package com.photoconnect.service;

import com.photoconnect.entity.Review;
import com.photoconnect.entity.ReviewStatus;

import java.util.List;

public interface AdminReviewService {

    /**
     * Lists all client reviews for administrative oversight with booking, customer,
     * and photographer profile entities eagerly fetched.
     */
    List<Review> listReviews();

    /**
     * Lists client reviews filtered by their visibility status.
     */
    List<Review> listReviewsByStatus(ReviewStatus status);

    /**
     * Hides a review from public visibility and recalculates the photographer's rating metrics.
     */
    Review hideReview(Long reviewId);

    /**
     * Restores a previously hidden review to visible status and recalculates rating metrics.
     */
    Review unhideReview(Long reviewId);

    /**
     * Counts reviews by status.
     */
    long countByStatus(ReviewStatus status);

    /**
     * Counts all reviews.
     */
    long countAll();
}
