package com.photoconnect.service;

import com.photoconnect.entity.Review;

import java.util.List;

public interface AdminReviewService {

    /**
     * Lists all client reviews for administrative oversight with booking, customer,
     * and photographer profile entities eagerly fetched.
     */
    List<Review> listReviews();
}
