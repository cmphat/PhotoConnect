package com.photoconnect.service;

import com.photoconnect.dto.ReviewDto;
import com.photoconnect.dto.ReviewRequest;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.Review;
import com.photoconnect.exception.BookingNotCompletedException;
import com.photoconnect.exception.InvalidBookingException;
import com.photoconnect.exception.InvalidReviewException;
import com.photoconnect.exception.ReviewAccessDeniedException;
import com.photoconnect.exception.ReviewAlreadyExistsException;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final PhotographerProfileRepository photographerProfileRepository;

    public ReviewServiceImpl(BookingRepository bookingRepository,
                             ReviewRepository reviewRepository,
                             PhotographerProfileRepository photographerProfileRepository) {
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
        this.photographerProfileRepository = photographerProfileRepository;
    }

    @Override
    public ReviewDto createReview(Long bookingId, Long sessionUserId, ReviewRequest request) {
        if (bookingId == null || sessionUserId == null) {
            throw new IllegalArgumentException("Booking ID and Session User ID must not be null.");
        }
        if (request == null) {
            throw new InvalidReviewException("Review request must not be null.");
        }

        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new InvalidBookingException("Booking not found with ID: " + bookingId));

        // 1. Authorization: Only the booking's customer can review
        if (!booking.getCustomer().getId().equals(sessionUserId)) {
            throw new ReviewAccessDeniedException("You are not authorized to review this booking.");
        }

        // 2. Status: Must be COMPLETED
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BookingNotCompletedException("Only completed bookings can be reviewed.");
        }

        // 3. Uniqueness: Cannot review the same booking twice
        if (reviewRepository.existsByBookingId(bookingId)) {
            throw new ReviewAlreadyExistsException("A review has already been submitted for this booking.");
        }

        // 4. Rating validation
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new InvalidReviewException("Rating must be between 1 and 5.");
        }

        // 5. Comment validation and trimming
        String trimmedComment = null;
        if (request.getComment() != null) {
            trimmedComment = request.getComment().trim();
            if (trimmedComment.length() > 1000) {
                throw new InvalidReviewException("Comment cannot exceed 1000 characters.");
            }
            if (trimmedComment.isEmpty()) {
                trimmedComment = null;
            }
        }

        // 6. Save Review with customer and photographerProfile derived from booking
        Review review = new Review(
                booking,
                booking.getCustomer(),
                booking.getPhotographerProfile(),
                request.getRating(),
                trimmedComment
        );
        review = reviewRepository.save(review);

        // 7. Recalculate photographer rating stats
        recalculatePhotographerRating(booking.getPhotographerProfile().getId());

        return ReviewDto.from(review);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewDto getReviewByBookingId(Long bookingId, Long sessionUserId) {
        if (bookingId == null) {
            return null;
        }
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new InvalidBookingException("Booking not found with ID: " + bookingId));

        if (sessionUserId != null) {
            boolean isCustomer = booking.getCustomer().getId().equals(sessionUserId);
            boolean isPhotographer = booking.getPhotographerProfile() != null
                    && booking.getPhotographerProfile().getUser() != null
                    && booking.getPhotographerProfile().getUser().getId().equals(sessionUserId);

            if (!isCustomer && !isPhotographer) {
                throw new ReviewAccessDeniedException("You are not authorized to view reviews for this booking.");
            }
        }

        return reviewRepository.findByBookingIdWithCustomer(bookingId)
                .map(ReviewDto::from)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsForPhotographer(Long photographerProfileId) {
        if (photographerProfileId == null) {
            return List.of();
        }
        return reviewRepository.findByPhotographerProfileIdWithCustomer(photographerProfileId)
                .stream()
                .map(ReviewDto::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasReviewForBooking(Long bookingId) {
        if (bookingId == null) {
            return false;
        }
        return reviewRepository.existsByBookingId(bookingId);
    }

    private void recalculatePhotographerRating(Long profileId) {
        PhotographerProfile profile = photographerProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Photographer profile not found: " + profileId));

        List<Object[]> statsList = reviewRepository.getRatingStatsByProfileId(profileId);
        if (statsList != null && !statsList.isEmpty() && statsList.get(0) != null && statsList.get(0)[0] != null) {
            Object[] stats = statsList.get(0);
            Double avg = stats[0] != null ? ((Number) stats[0]).doubleValue() : 0.0;
            Long count = stats[1] != null ? ((Number) stats[1]).longValue() : 0L;
            double rounded = Math.round(avg * 100.0) / 100.0;
            profile.setAverageRating(rounded);
            profile.setReviewCount(count.intValue());
        } else {
            List<Review> allReviews = reviewRepository.findByPhotographerProfileIdWithCustomer(profileId);
            if (!allReviews.isEmpty()) {
                double avg = allReviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
                profile.setAverageRating(Math.round(avg * 100.0) / 100.0);
                profile.setReviewCount(allReviews.size());
            } else {
                profile.setAverageRating(0.0);
                profile.setReviewCount(0);
            }
        }

        photographerProfileRepository.save(profile);
    }
}
