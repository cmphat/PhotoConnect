package com.photoconnect.repository;

import com.photoconnect.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("""
            SELECT r FROM Review r
            JOIN FETCH r.customer
            WHERE r.booking.id = :bookingId
            """)
    Optional<Review> findByBookingIdWithCustomer(@Param("bookingId") Long bookingId);

    Optional<Review> findByBookingId(Long bookingId);

    boolean existsByBookingId(Long bookingId);

    @Query("""
            SELECT r FROM Review r
            JOIN FETCH r.customer
            WHERE r.photographerProfile.id = :profileId
            ORDER BY r.createdAt DESC
            """)
    List<Review> findByPhotographerProfileIdWithCustomer(@Param("profileId") Long profileId);

    @Query("""
            SELECT AVG(1.0 * r.rating), COUNT(r)
            FROM Review r
            WHERE r.photographerProfile.id = :profileId
            """)
    List<Object[]> getRatingStatsByProfileId(@Param("profileId") Long profileId);

    @Query("""
            SELECT r FROM Review r
            JOIN FETCH r.booking b
            JOIN FETCH r.customer
            JOIN FETCH r.photographerProfile p
            ORDER BY r.createdAt DESC
            """)
    List<Review> findAllWithDetails();
}
