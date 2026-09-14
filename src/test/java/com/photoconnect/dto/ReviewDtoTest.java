package com.photoconnect.dto;

import com.photoconnect.entity.Booking;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.Review;
import com.photoconnect.entity.ReviewStatus;
import com.photoconnect.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReviewDtoTest {

    @Test
    void from_withNull_returnsNull() {
        assertNull(ReviewDto.from(null));
    }

    @Test
    void from_withVisibleReview_mapsFieldsCorrectly() {
        Booking booking = new Booking();
        booking.setId(10L);

        User customer = new User();
        customer.setFullName("Jane Doe");

        PhotographerProfile profile = new PhotographerProfile();
        profile.setId(20L);

        Review review = new Review(booking, customer, profile, 5, "Brilliant photos!", ReviewStatus.VISIBLE);
        review.setId(1L);

        ReviewDto dto = ReviewDto.from(review);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(10L, dto.getBookingId());
        assertEquals(20L, dto.getPhotographerProfileId());
        assertEquals(5, dto.getRating());
        assertEquals("Brilliant photos!", dto.getComment());
        assertEquals("Jane Doe", dto.getCustomerName());
        assertEquals(ReviewStatus.VISIBLE, dto.getStatus());
        assertFalse(dto.isHidden());
    }

    @Test
    void from_withHiddenReview_mapsHiddenStatusCorrectly() {
        Booking booking = new Booking();
        booking.setId(10L);

        User customer = new User();
        customer.setFullName("John Smith");

        PhotographerProfile profile = new PhotographerProfile();
        profile.setId(20L);

        Review review = new Review(booking, customer, profile, 1, "Inappropriate comment", ReviewStatus.HIDDEN);
        review.setId(2L);

        ReviewDto dto = ReviewDto.from(review);

        assertNotNull(dto);
        assertEquals(ReviewStatus.HIDDEN, dto.getStatus());
        assertTrue(dto.isHidden());
    }

    @Test
    void constructor_defaultsToVisible() {
        ReviewDto dto = new ReviewDto(1L, 2L, 3L, 4, "Nice", "User", LocalDateTime.now());
        assertEquals(ReviewStatus.VISIBLE, dto.getStatus());
        assertFalse(dto.isHidden());
    }
}
