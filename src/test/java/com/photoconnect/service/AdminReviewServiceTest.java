package com.photoconnect.service;

import com.photoconnect.entity.Review;
import com.photoconnect.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    private AdminReviewService adminReviewService;

    @BeforeEach
    void setUp() {
        adminReviewService = new AdminReviewServiceImpl(reviewRepository);
    }

    @Test
    void listReviews_shouldCallFindAllWithDetails() {
        Review review = new Review();
        review.setId(1L);
        review.setRating(5);
        when(reviewRepository.findAllWithDetails()).thenReturn(List.of(review));

        List<Review> result = adminReviewService.listReviews();

        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getRating());
        verify(reviewRepository).findAllWithDetails();
    }
}
