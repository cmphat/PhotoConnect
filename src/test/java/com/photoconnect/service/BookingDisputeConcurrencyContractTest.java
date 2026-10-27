package com.photoconnect.service;

import com.photoconnect.entity.Booking;
import jakarta.persistence.Version;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BookingDisputeConcurrencyContractTest {
    @Test
    void bookingUsesOptimisticVersionForCompetingResolutionTransactions() throws Exception {
        assertThat(Booking.class.getDeclaredField("version").isAnnotationPresent(Version.class)).isTrue();
    }
}
