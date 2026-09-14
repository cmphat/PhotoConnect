package com.photoconnect.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReviewTest {

    @Test
    void defaultConstructor_setsStatusVisible() {
        Review review = new Review();
        assertEquals(ReviewStatus.VISIBLE, review.getStatus());
    }

    @Test
    void fiveArgConstructor_setsStatusVisible() {
        Review review = new Review(new Booking(), new User(), new PhotographerProfile(), 5, "Great");
        assertEquals(ReviewStatus.VISIBLE, review.getStatus());
    }

    @Test
    void sixArgConstructor_setsExplicitStatus() {
        Review review = new Review(new Booking(), new User(), new PhotographerProfile(), 4, "Nice", ReviewStatus.HIDDEN);
        assertEquals(ReviewStatus.HIDDEN, review.getStatus());

        Review reviewNullStatus = new Review(new Booking(), new User(), new PhotographerProfile(), 4, "Nice", null);
        assertEquals(ReviewStatus.VISIBLE, reviewNullStatus.getStatus());
    }

    @Test
    void setStatus_updatesStatus() {
        Review review = new Review();
        review.setStatus(ReviewStatus.HIDDEN);
        assertEquals(ReviewStatus.HIDDEN, review.getStatus());

        review.setStatus(ReviewStatus.VISIBLE);
        assertEquals(ReviewStatus.VISIBLE, review.getStatus());
    }

    @Test
    void onCreate_initializesNullStatus() {
        Review review = new Review();
        review.setStatus(null);
        review.onCreate();

        assertEquals(ReviewStatus.VISIBLE, review.getStatus());
        assertNotNull(review.getCreatedAt());
    }
}
