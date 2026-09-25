package com.photoconnect.repository;

import com.photoconnect.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.customer
            JOIN FETCH b.photographerProfile p
            JOIN FETCH p.user
            WHERE b.customer.id = :customerId
            ORDER BY b.createdAt DESC
            """)
    List<Booking> findByCustomerIdWithDetails(@Param("customerId") Long customerId);

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.customer
            JOIN FETCH b.photographerProfile p
            JOIN FETCH p.user
            WHERE p.user.id = :photographerUserId
            ORDER BY b.createdAt DESC
            """)
    List<Booking> findByPhotographerUserIdWithDetails(@Param("photographerUserId") Long photographerUserId);

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.customer
            JOIN FETCH b.photographerProfile p
            JOIN FETCH p.user
            WHERE b.id = :id
            """)
    Optional<Booking> findByIdWithDetails(@Param("id") Long id);

    long countByStatus(com.photoconnect.entity.BookingStatus status);

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.customer
            JOIN FETCH b.photographerProfile p
            JOIN FETCH p.user
            WHERE (:status IS NULL OR b.status = :status)
            ORDER BY b.createdAt DESC
            """)
    List<Booking> findAllWithDetails(@Param("status") com.photoconnect.entity.BookingStatus status);

    long countByCustomerIdAndStatus(Long customerId, com.photoconnect.entity.BookingStatus status);

    @Query("""
            SELECT COUNT(b) FROM Booking b
            WHERE b.customer.id = :customerId
              AND b.bookingDate >= :today
              AND b.status IN :statuses
            """)
    long countUpcomingForCustomer(@Param("customerId") Long customerId,
                                  @Param("today") java.time.LocalDate today,
                                  @Param("statuses") List<com.photoconnect.entity.BookingStatus> statuses);

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.customer
            JOIN FETCH b.photographerProfile p
            JOIN FETCH p.user
            WHERE b.customer.id = :customerId
            ORDER BY
              CASE WHEN b.bookingDate >= :today AND b.status IN :activeStatuses THEN 0 ELSE 1 END,
              CASE WHEN b.bookingDate >= :today AND b.status IN :activeStatuses THEN b.bookingDate ELSE NULL END ASC,
              b.updatedAt DESC,
              b.createdAt DESC
            """)
    List<Booking> findDashboardBookings(@Param("customerId") Long customerId,
                                        @Param("today") java.time.LocalDate today,
                                        @Param("activeStatuses") List<com.photoconnect.entity.BookingStatus> activeStatuses,
                                        Pageable pageable);

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.customer
            JOIN FETCH b.photographerProfile p
            JOIN FETCH p.user
            WHERE b.customer.id = :customerId
              AND (
                b.status = com.photoconnect.entity.BookingStatus.ACCEPTED
                OR (
                  b.status = com.photoconnect.entity.BookingStatus.COMPLETED
                  AND NOT EXISTS (SELECT r.id FROM Review r WHERE r.booking = b)
                )
              )
            ORDER BY b.bookingDate ASC, b.bookingTime ASC
            """)
    List<Booking> findDashboardAttentionCandidates(@Param("customerId") Long customerId);

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.customer
            JOIN FETCH b.photographerProfile p
            WHERE p.id = :profileId
              AND b.status = com.photoconnect.entity.BookingStatus.PENDING
            ORDER BY b.createdAt ASC, b.id ASC
            """)
    List<Booking> findPendingStudioRequests(@Param("profileId") Long profileId, Pageable pageable);

    long countByPhotographerProfileIdAndStatus(Long profileId,
                                               com.photoconnect.entity.BookingStatus status);

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.customer
            JOIN FETCH b.photographerProfile p
            WHERE p.id = :profileId
              AND b.status = com.photoconnect.entity.BookingStatus.ACCEPTED
              AND b.bookingDate >= :today
            ORDER BY b.bookingDate ASC, b.bookingTime ASC, b.id ASC
            """)
    List<Booking> findUpcomingAcceptedForStudio(@Param("profileId") Long profileId,
                                                @Param("today") java.time.LocalDate today,
                                                Pageable pageable);
}
