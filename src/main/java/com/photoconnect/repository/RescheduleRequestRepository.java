package com.photoconnect.repository;

import com.photoconnect.entity.RescheduleRequest;
import com.photoconnect.entity.RescheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RescheduleRequestRepository extends JpaRepository<RescheduleRequest, Long> {

    @Query("SELECT r FROM RescheduleRequest r JOIN FETCH r.requestedBy JOIN FETCH r.booking b JOIN FETCH b.photographerProfile p JOIN FETCH p.user pu JOIN FETCH b.customer c WHERE r.id = :id")
    Optional<RescheduleRequest> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT r FROM RescheduleRequest r JOIN FETCH r.requestedBy WHERE r.booking.id = :bookingId ORDER BY r.createdAt DESC")
    List<RescheduleRequest> findByBookingIdWithDetails(@Param("bookingId") Long bookingId);

    Optional<RescheduleRequest> findFirstByBookingIdAndStatus(Long bookingId, RescheduleStatus status);

    boolean existsByBookingIdAndStatus(Long bookingId, RescheduleStatus status);

    List<RescheduleRequest> findByBookingIdAndStatus(Long bookingId, RescheduleStatus status);
}
