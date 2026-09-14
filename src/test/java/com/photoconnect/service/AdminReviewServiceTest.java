package com.photoconnect.service;

import com.photoconnect.entity.Booking;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.Review;
import com.photoconnect.entity.ReviewStatus;
import com.photoconnect.entity.User;
import com.photoconnect.exception.ReviewNotFoundException;
import com.photoconnect.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewService reviewService;

    private AdminReviewService adminReviewService;

    @BeforeEach
    void setUp() {
        adminReviewService = new AdminReviewServiceImpl(reviewRepository, reviewService);
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

    @Test
    void listReviewsByStatus_withNull_shouldCallFindAllWithDetails() {
        Review review = new Review();
        review.setId(1L);
        when(reviewRepository.findAllWithDetails()).thenReturn(List.of(review));

        List<Review> result = adminReviewService.listReviewsByStatus(null);

        assertEquals(1, result.size());
        verify(reviewRepository).findAllWithDetails();
    }

    @Test
    void listReviewsByStatus_withVisible_shouldCallFindByStatusWithDetails() {
        Review review = new Review();
        review.setId(2L);
        review.setStatus(ReviewStatus.VISIBLE);
        when(reviewRepository.findByStatusWithDetails(ReviewStatus.VISIBLE)).thenReturn(List.of(review));

        List<Review> result = adminReviewService.listReviewsByStatus(ReviewStatus.VISIBLE);

        assertEquals(1, result.size());
        assertEquals(ReviewStatus.VISIBLE, result.get(0).getStatus());
        verify(reviewRepository).findByStatusWithDetails(ReviewStatus.VISIBLE);
    }

    @Test
    void listReviewsByStatus_withHidden_shouldCallFindByStatusWithDetails() {
        Review review = new Review();
        review.setId(3L);
        review.setStatus(ReviewStatus.HIDDEN);
        when(reviewRepository.findByStatusWithDetails(ReviewStatus.HIDDEN)).thenReturn(List.of(review));

        List<Review> result = adminReviewService.listReviewsByStatus(ReviewStatus.HIDDEN);

        assertEquals(1, result.size());
        assertEquals(ReviewStatus.HIDDEN, result.get(0).getStatus());
        verify(reviewRepository).findByStatusWithDetails(ReviewStatus.HIDDEN);
    }

    @Test
    void hideReview_shouldSetStatusHidden_saveAndRecalculate() {
        PhotographerProfile profile = new PhotographerProfile();
        profile.setId(10L);

        Review review = new Review();
        review.setId(5L);
        review.setStatus(ReviewStatus.VISIBLE);
        review.setPhotographerProfile(profile);

        when(reviewRepository.findByIdWithDetails(5L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        Review result = adminReviewService.hideReview(5L);

        assertEquals(ReviewStatus.HIDDEN, result.getStatus());
        verify(reviewRepository).save(review);
        verify(reviewService).recalculatePhotographerRating(10L);
    }

    @Test
    void hideReview_withNullId_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> adminReviewService.hideReview(null));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void hideReview_withNonExistentId_shouldThrowNotFoundException() {
        when(reviewRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        assertThrows(ReviewNotFoundException.class, () -> adminReviewService.hideReview(999L));
        verify(reviewRepository, never()).save(any());
        verify(reviewService, never()).recalculatePhotographerRating(any());
    }

    @Test
    void unhideReview_shouldSetStatusVisible_saveAndRecalculate() {
        PhotographerProfile profile = new PhotographerProfile();
        profile.setId(20L);

        Review review = new Review();
        review.setId(7L);
        review.setStatus(ReviewStatus.HIDDEN);
        review.setPhotographerProfile(profile);

        when(reviewRepository.findByIdWithDetails(7L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        Review result = adminReviewService.unhideReview(7L);

        assertEquals(ReviewStatus.VISIBLE, result.getStatus());
        verify(reviewRepository).save(review);
        verify(reviewService).recalculatePhotographerRating(20L);
    }

    @Test
    void unhideReview_withNullId_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> adminReviewService.unhideReview(null));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void unhideReview_withNonExistentId_shouldThrowNotFoundException() {
        when(reviewRepository.findByIdWithDetails(888L)).thenReturn(Optional.empty());

        assertThrows(ReviewNotFoundException.class, () -> adminReviewService.unhideReview(888L));
        verify(reviewRepository, never()).save(any());
        verify(reviewService, never()).recalculatePhotographerRating(any());
    }

    @Test
    void countByStatus_shouldDelegateToRepository() {
        when(reviewRepository.countByStatus(ReviewStatus.VISIBLE)).thenReturn(10L);
        when(reviewRepository.countByStatus(ReviewStatus.HIDDEN)).thenReturn(2L);
        when(reviewRepository.count()).thenReturn(12L);

        assertEquals(10L, adminReviewService.countByStatus(ReviewStatus.VISIBLE));
        assertEquals(2L, adminReviewService.countByStatus(ReviewStatus.HIDDEN));
        assertEquals(12L, adminReviewService.countByStatus(null));
        assertEquals(12L, adminReviewService.countAll());

        verify(reviewRepository).countByStatus(ReviewStatus.VISIBLE);
        verify(reviewRepository).countByStatus(ReviewStatus.HIDDEN);
        verify(reviewRepository, times(2)).count();
    }
}
