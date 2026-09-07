package com.photoconnect.repository;

import com.photoconnect.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("""
            SELECT m FROM Message m
            JOIN FETCH m.sender
            JOIN FETCH m.receiver
            WHERE m.booking.id = :bookingId
            ORDER BY m.sentAt ASC
            """)
    List<Message> findByBookingIdWithDetails(@Param("bookingId") Long bookingId);

    long countByBookingIdAndReceiverIdAndIsReadFalse(Long bookingId, Long receiverId);

    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.booking.id = :bookingId AND m.receiver.id = :receiverId AND m.isRead = false")
    int markMessagesAsRead(@Param("bookingId") Long bookingId, @Param("receiverId") Long receiverId);
}
