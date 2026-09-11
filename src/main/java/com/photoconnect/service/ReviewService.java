package com.photoconnect.service;

import com.photoconnect.dto.ReviewDto;
import com.photoconnect.dto.ReviewRequest;

import java.util.List;

public interface ReviewService {

    ReviewDto createReview(Long bookingId, Long sessionUserId, ReviewRequest request);

    ReviewDto getReviewByBookingId(Long bookingId, Long sessionUserId);

    List<ReviewDto> getReviewsForPhotographer(Long photographerProfileId);

    boolean hasReviewForBooking(Long bookingId);
}
