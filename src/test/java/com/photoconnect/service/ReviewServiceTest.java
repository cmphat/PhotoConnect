package com.photoconnect.service;

import com.photoconnect.dto.ReviewDto;
import com.photoconnect.dto.ReviewRequest;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.Review;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.exception.BookingNotCompletedException;
import com.photoconnect.exception.InvalidReviewException;
import com.photoconnect.exception.ReviewAccessDeniedException;
import com.photoconnect.exception.ReviewAlreadyExistsException;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private PhotographerProfileRepository photographerProfileRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User customer;
    private User photographerUser;
    private PhotographerProfile photographerProfile;
    private Booking booking;

    @BeforeEach
    void setUp() {
        customer = new User();
        customer.setId(100L);
        customer.setFullName("Alice Customer");
        customer.setEmail("alice@example.com");
        customer.setRole(UserRole.CUSTOMER);
        customer.setStatus(UserStatus.ACTIVE);

        photographerUser = new User();
        photographerUser.setId(200L);
        photographerUser.setFullName("Bob Photographer");
        photographerUser.setEmail("bob@example.com");
        photographerUser.setRole(UserRole.PHOTOGRAPHER);
        photographerUser.setStatus(UserStatus.ACTIVE);

        photographerProfile = new PhotographerProfile();
        photographerProfile.setId(10L);
        photographerProfile.setUser(photographerUser);
        photographerProfile.setDisplayName("Bob Photography");
        photographerProfile.setPriceFrom(new BigDecimal("2000000"));
        photographerProfile.setAverageRating(0.0);
        photographerProfile.setReviewCount(0);

        booking = new Booking(customer, photographerProfile, LocalDate.now().minusDays(1), LocalTime.of(10, 0), "Central Park", "Portrait shoot", new BigDecimal("2000000"));
        booking.setId(1L);
        booking.setStatus(BookingStatus.COMPLETED);
    }

    @Test
    void createReview_validCompletedBooking_shouldSucceed() {
        when(bookingRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBookingId(1L)).thenReturn(false);
        when(photographerProfileRepository.findById(10L)).thenReturn(Optional.of(photographerProfile));

        Review savedReview = new Review(booking, customer, photographerProfile, 5, "Great experience!");
        savedReview.setId(50L);
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        Object[] stats = new Object[]{5.0, 1L};
        when(reviewRepository.getRatingStatsByProfileId(10L)).thenReturn(List.<Object[]>of(stats));

        ReviewRequest request = new ReviewRequest(5, "Great experience!");
        ReviewDto result = reviewService.createReview(1L, 100L, request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(50L);
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getComment()).isEqualTo("Great experience!");
        assertThat(result.getCustomerName()).isEqualTo("Alice Customer");

        verify(reviewRepository).save(any(Review.class));
        verify(photographerProfileRepository).save(photographerProfile);
        assertThat(photographerProfile.getAverageRating()).isEqualTo(5.0);
        assertThat(photographerProfile.getReviewCount()).isEqualTo(1);
    }

    @Test
    void createReview_pendingBooking_shouldThrowBookingNotCompletedException() {
        booking.setStatus(BookingStatus.PENDING);
        when(bookingRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(booking));

        ReviewRequest request = new ReviewRequest(5, "Good shoot");

        assertThatThrownBy(() -> reviewService.createReview(1L, 100L, request))
                .isInstanceOf(BookingNotCompletedException.class)
                .hasMessageContaining("Only completed bookings can be reviewed");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_acceptedBooking_shouldThrowBookingNotCompletedException() {
        booking.setStatus(BookingStatus.ACCEPTED);
        when(bookingRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(booking));

        ReviewRequest request = new ReviewRequest(4, "Nice");

        assertThatThrownBy(() -> reviewService.createReview(1L, 100L, request))
                .isInstanceOf(BookingNotCompletedException.class)
                .hasMessageContaining("Only completed bookings can be reviewed");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_unauthorizedCustomer_shouldThrowReviewAccessDeniedException() {
        when(bookingRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(booking));

        ReviewRequest request = new ReviewRequest(5, "I was not the customer");

        assertThatThrownBy(() -> reviewService.createReview(1L, 999L, request))
                .isInstanceOf(ReviewAccessDeniedException.class)
                .hasMessageContaining("not authorized");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_duplicateReview_shouldThrowReviewAlreadyExistsException() {
        when(bookingRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBookingId(1L)).thenReturn(true);

        ReviewRequest request = new ReviewRequest(5, "Second review");

        assertThatThrownBy(() -> reviewService.createReview(1L, 100L, request))
                .isInstanceOf(ReviewAlreadyExistsException.class)
                .hasMessageContaining("already been submitted");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_ratingLessThanOne_shouldThrowInvalidReviewException() {
        when(bookingRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBookingId(1L)).thenReturn(false);

        ReviewRequest request = new ReviewRequest(0, "Invalid rating");

        assertThatThrownBy(() -> reviewService.createReview(1L, 100L, request))
                .isInstanceOf(InvalidReviewException.class)
                .hasMessageContaining("Rating must be between 1 and 5");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_ratingGreaterThanFive_shouldThrowInvalidReviewException() {
        when(bookingRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBookingId(1L)).thenReturn(false);

        ReviewRequest request = new ReviewRequest(6, "Invalid rating");

        assertThatThrownBy(() -> reviewService.createReview(1L, 100L, request))
                .isInstanceOf(InvalidReviewException.class)
                .hasMessageContaining("Rating must be between 1 and 5");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_blankComment_shouldBeAllowedAndSavedAsNull() {
        when(bookingRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBookingId(1L)).thenReturn(false);
        when(photographerProfileRepository.findById(10L)).thenReturn(Optional.of(photographerProfile));

        Review savedReview = new Review(booking, customer, photographerProfile, 4, null);
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        ReviewRequest request = new ReviewRequest(4, "   ");
        ReviewDto result = reviewService.createReview(1L, 100L, request);

        assertThat(result).isNotNull();
        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(captor.capture());
        assertThat(captor.getValue().getComment()).isNull();
    }

    @Test
    void createReview_overlongComment_shouldThrowInvalidReviewException() {
        when(bookingRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBookingId(1L)).thenReturn(false);

        String longComment = "A".repeat(1001);
        ReviewRequest request = new ReviewRequest(5, longComment);

        assertThatThrownBy(() -> reviewService.createReview(1L, 100L, request))
                .isInstanceOf(InvalidReviewException.class)
                .hasMessageContaining("Comment cannot exceed 1000 characters");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_shouldRecalculateAverageRatingAndReviewCountCorrectly() {
        when(bookingRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBookingId(1L)).thenReturn(false);
        when(photographerProfileRepository.findById(10L)).thenReturn(Optional.of(photographerProfile));

        Review savedReview = new Review(booking, customer, photographerProfile, 4, "Very good");
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        // Simulated aggregated stats: 2 reviews total, average 4.5
        Object[] stats = new Object[]{4.5, 2L};
        when(reviewRepository.getRatingStatsByProfileId(10L)).thenReturn(List.<Object[]>of(stats));

        ReviewRequest request = new ReviewRequest(4, "Very good");
        reviewService.createReview(1L, 100L, request);

        ArgumentCaptor<PhotographerProfile> profileCaptor = ArgumentCaptor.forClass(PhotographerProfile.class);
        verify(photographerProfileRepository).save(profileCaptor.capture());

        PhotographerProfile updatedProfile = profileCaptor.getValue();
        assertThat(updatedProfile.getAverageRating()).isEqualTo(4.5);
        assertThat(updatedProfile.getReviewCount()).isEqualTo(2);
    }

    @Test
    void getReviewByBookingId_authorizedCustomer_shouldReturnDto() {
        when(bookingRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(booking));
        Review review = new Review(booking, customer, photographerProfile, 5, "Amazing");
        review.setId(12L);
        when(reviewRepository.findByBookingIdWithCustomer(1L)).thenReturn(Optional.of(review));

        ReviewDto result = reviewService.getReviewByBookingId(1L, 100L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(12L);
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getComment()).isEqualTo("Amazing");
    }

    @Test
    void getReviewByBookingId_unauthorizedUser_shouldThrowReviewAccessDeniedException() {
        when(bookingRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> reviewService.getReviewByBookingId(1L, 999L))
                .isInstanceOf(ReviewAccessDeniedException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    void getReviewsForPhotographer_shouldReturnDtoList() {
        Review review1 = new Review(booking, customer, photographerProfile, 5, "Great");
        review1.setId(1L);
        Review review2 = new Review(booking, customer, photographerProfile, 4, "Good");
        review2.setId(2L);
        when(reviewRepository.findByPhotographerProfileIdWithCustomer(10L)).thenReturn(List.of(review1, review2));

        List<ReviewDto> results = reviewService.getReviewsForPhotographer(10L);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getRating()).isEqualTo(5);
        assertThat(results.get(1).getRating()).isEqualTo(4);
        assertThat(results.get(0).getStatus()).isEqualTo(com.photoconnect.entity.ReviewStatus.VISIBLE);
    }

    @Test
    void recalculatePhotographerRating_whenNoReviews_shouldResetToZero() {
        when(photographerProfileRepository.findById(10L)).thenReturn(Optional.of(photographerProfile));
        when(reviewRepository.getRatingStatsByProfileId(10L)).thenReturn(List.of());
        when(reviewRepository.findByPhotographerProfileIdWithCustomer(10L)).thenReturn(List.of());

        reviewService.recalculatePhotographerRating(10L);

        verify(photographerProfileRepository).save(photographerProfile);
        assertThat(photographerProfile.getAverageRating()).isEqualTo(0.0);
        assertThat(photographerProfile.getReviewCount()).isEqualTo(0);
    }

    @Test
    void recalculatePhotographerRating_whenReviewsExist_shouldUpdateAverageAndCount() {
        when(photographerProfileRepository.findById(10L)).thenReturn(Optional.of(photographerProfile));
        Object[] stats = new Object[]{4.75, 4L};
        when(reviewRepository.getRatingStatsByProfileId(10L)).thenReturn(List.<Object[]>of(stats));

        reviewService.recalculatePhotographerRating(10L);

        verify(photographerProfileRepository).save(photographerProfile);
        assertThat(photographerProfile.getAverageRating()).isEqualTo(4.75);
        assertThat(photographerProfile.getReviewCount()).isEqualTo(4);
    }
}
