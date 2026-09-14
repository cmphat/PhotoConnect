package com.photoconnect.service;

import com.photoconnect.entity.Review;
import com.photoconnect.entity.ReviewStatus;
import com.photoconnect.exception.ReviewNotFoundException;
import com.photoconnect.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AdminReviewServiceImpl implements AdminReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;

    public AdminReviewServiceImpl(ReviewRepository reviewRepository, ReviewService reviewService) {
        this.reviewRepository = reviewRepository;
        this.reviewService = reviewService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> listReviews() {
        return reviewRepository.findAllWithDetails();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> listReviewsByStatus(ReviewStatus status) {
        if (status == null) {
            return listReviews();
        }
        return reviewRepository.findByStatusWithDetails(status);
    }

    @Override
    public Review hideReview(Long reviewId) {
        if (reviewId == null) {
            throw new IllegalArgumentException("Review ID must not be null.");
        }
        Review review = reviewRepository.findByIdWithDetails(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with ID: " + reviewId));

        review.setStatus(ReviewStatus.HIDDEN);
        Review saved = reviewRepository.save(review);

        if (review.getPhotographerProfile() != null) {
            reviewService.recalculatePhotographerRating(review.getPhotographerProfile().getId());
        }

        return saved;
    }

    @Override
    public Review unhideReview(Long reviewId) {
        if (reviewId == null) {
            throw new IllegalArgumentException("Review ID must not be null.");
        }
        Review review = reviewRepository.findByIdWithDetails(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with ID: " + reviewId));

        review.setStatus(ReviewStatus.VISIBLE);
        Review saved = reviewRepository.save(review);

        if (review.getPhotographerProfile() != null) {
            reviewService.recalculatePhotographerRating(review.getPhotographerProfile().getId());
        }

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(ReviewStatus status) {
        if (status == null) {
            return countAll();
        }
        return reviewRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAll() {
        return reviewRepository.count();
    }
}
