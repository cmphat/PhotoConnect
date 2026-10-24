package com.photoconnect.repository;

import com.photoconnect.entity.BookingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingHistoryRepository extends JpaRepository<BookingHistory, Long> {

    @Query("""
            SELECT h FROM BookingHistory h
            JOIN FETCH h.changedBy
            WHERE h.booking.id = :bookingId
            ORDER BY h.changedAt ASC
            """)
    List<BookingHistory> findByBookingIdWithUserOrderByChangedAtAsc(@Param("bookingId") Long bookingId);

    List<BookingHistory> findByBookingIdOrderByChangedAtAsc(Long bookingId);
}
