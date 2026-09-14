package com.photoconnect.service;

import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminBookingServiceImpl implements AdminBookingService {

    private final BookingRepository bookingRepository;

    public AdminBookingServiceImpl(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> listBookings(BookingStatus status) {
        return bookingRepository.findAllWithDetails(status);
    }
}
