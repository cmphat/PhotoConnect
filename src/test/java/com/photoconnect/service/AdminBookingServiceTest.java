package com.photoconnect.service;

import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.repository.BookingRepository;
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
class AdminBookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    private AdminBookingService adminBookingService;

    @BeforeEach
    void setUp() {
        adminBookingService = new AdminBookingServiceImpl(bookingRepository);
    }

    @Test
    void listBookings_nullStatus_shouldCallFindAllWithDetailsNull() {
        Booking b = new Booking();
        b.setId(1L);
        when(bookingRepository.findAllWithDetails(null)).thenReturn(List.of(b));

        List<Booking> result = adminBookingService.listBookings(null);

        assertEquals(1, result.size());
        verify(bookingRepository).findAllWithDetails(null);
    }

    @Test
    void listBookings_withStatus_shouldFilterByStatus() {
        Booking b = new Booking();
        b.setId(2L);
        b.setStatus(BookingStatus.COMPLETED);
        when(bookingRepository.findAllWithDetails(BookingStatus.COMPLETED)).thenReturn(List.of(b));

        List<Booking> result = adminBookingService.listBookings(BookingStatus.COMPLETED);

        assertEquals(1, result.size());
        assertEquals(BookingStatus.COMPLETED, result.get(0).getStatus());
        verify(bookingRepository).findAllWithDetails(BookingStatus.COMPLETED);
    }
}
