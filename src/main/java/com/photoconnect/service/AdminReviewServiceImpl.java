package com.photoconnect.service;

import com.photoconnect.entity.Review;
import com.photoconnect.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminReviewServiceImpl implements AdminReviewService {

    private final ReviewRepository reviewRepository;

    public AdminReviewServiceImpl(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> listReviews() {
        return reviewRepository.findAllWithDetails();
    }
}
