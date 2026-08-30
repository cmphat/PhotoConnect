package com.photoconnect.repository;

import com.photoconnect.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Booking> findByPhotographerProfileIdOrderByCreatedAtDesc(Long photographerProfileId);

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.customer
            JOIN FETCH b.photographerProfile p
            JOIN FETCH p.user
            WHERE b.id = :id
            """)
    Optional<Booking> findByIdWithDetails(@Param("id") Long id);
}
