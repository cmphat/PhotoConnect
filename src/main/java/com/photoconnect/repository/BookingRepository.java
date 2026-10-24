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

    @Query("""
            SELECT COUNT(b) FROM Booking b
            WHERE b.photographerProfile.id = :profileId
              AND b.status = com.photoconnect.entity.BookingStatus.ACCEPTED
              AND b.bookingDate >= :today
            """)
    long countUpcomingAcceptedForStudio(@Param("profileId") Long profileId,
                                        @Param("today") java.time.LocalDate today);

    List<Booking> findByPhotographerProfileIdOrderByBookingDateAscBookingTimeAsc(Long profileId);

    List<Booking> findByCustomerIdOrderByBookingDateDesc(Long customerId);

    long countByPhotographerProfileId(Long profileId);

    long countByCustomerId(Long customerId);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.photographerProfile.id = :profileId
              AND b.bookingDate = :date
              AND b.status IN :reservingStatuses
              AND (:excludeBookingId IS NULL OR b.id <> :excludeBookingId)
            """)
    List<Booking> findActiveBookingsOnDate(@Param("profileId") Long profileId,
                                          @Param("date") java.time.LocalDate date,
                                          @Param("reservingStatuses") List<com.photoconnect.entity.BookingStatus> reservingStatuses,
                                          @Param("excludeBookingId") Long excludeBookingId);

    @Query("""
            SELECT COALESCE(SUM(b.agreedPrice), 0) FROM Booking b
            WHERE b.photographerProfile.id = :profileId
              AND b.status NOT IN (com.photoconnect.entity.BookingStatus.CANCELLED, com.photoconnect.entity.BookingStatus.REJECTED)
            """)
    java.math.BigDecimal calculateGrossBookingValue(@Param("profileId") Long profileId);

    @Query("""
            SELECT COALESCE(SUM(b.agreedPrice), 0) FROM Booking b
            WHERE b.photographerProfile.id = :profileId
              AND b.status = com.photoconnect.entity.BookingStatus.COMPLETED
            """)
    java.math.BigDecimal calculateCompletedRevenue(@Param("profileId") Long profileId);

    @Query("""
            SELECT COUNT(b) FROM Booking b
            WHERE b.photographerProfile.id = :profileId
              AND b.status = com.photoconnect.entity.BookingStatus.COMPLETED
            """)
    long countCompletedBookingsForPhotographer(@Param("profileId") Long profileId);

    @Query("""
            SELECT b.status, COUNT(b) FROM Booking b
            WHERE b.photographerProfile.id = :profileId
            GROUP BY b.status
            """)
    List<Object[]> countBookingsByStatusForPhotographer(@Param("profileId") Long profileId);

    @Query("""
            SELECT COALESCE(b.packageName, 'Custom Session'), COUNT(b), COALESCE(SUM(b.agreedPrice), 0)
            FROM Booking b
            WHERE b.photographerProfile.id = :profileId
              AND b.status NOT IN (com.photoconnect.entity.BookingStatus.CANCELLED, com.photoconnect.entity.BookingStatus.REJECTED)
            GROUP BY COALESCE(b.packageName, 'Custom Session')
            ORDER BY COUNT(b) DESC
            """)
    List<Object[]> findPopularPackagesForPhotographer(@Param("profileId") Long profileId);

    @Query("""
            SELECT b.bookingDate, b.status, b.agreedPrice
            FROM Booking b
            WHERE b.photographerProfile.id = :profileId
              AND b.bookingDate >= :startDate
            ORDER BY b.bookingDate ASC
            """)
    List<Object[]> findBookingTrendsSince(@Param("profileId") Long profileId, @Param("startDate") java.time.LocalDate startDate);

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.customer c
            JOIN FETCH b.photographerProfile p
            JOIN FETCH p.user u
            WHERE (:status IS NULL OR b.status = :status)
              AND (:startDate IS NULL OR b.bookingDate >= :startDate)
              AND (:endDate IS NULL OR b.bookingDate <= :endDate)
              AND (:search IS NULL OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(p.displayName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(b.location) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY b.createdAt DESC
            """)
    List<Booking> findAdminBookings(@Param("status") com.photoconnect.entity.BookingStatus status,
                                   @Param("search") String search,
                                   @Param("startDate") java.time.LocalDate startDate,
                                   @Param("endDate") java.time.LocalDate endDate);

    @Query("""
            SELECT COALESCE(SUM(b.agreedPrice), 0) FROM Booking b
            WHERE b.status NOT IN (com.photoconnect.entity.BookingStatus.CANCELLED, com.photoconnect.entity.BookingStatus.REJECTED)
            """)
    java.math.BigDecimal calculateTotalPlatformBookingVolume();

    @Query("""
            SELECT COALESCE(SUM(b.agreedPrice), 0) FROM Booking b
            WHERE b.status = com.photoconnect.entity.BookingStatus.COMPLETED
            """)
    java.math.BigDecimal calculateTotalPlatformCompletedRevenue();
}
