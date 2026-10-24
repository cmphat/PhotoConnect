package com.photoconnect.repository;

import com.photoconnect.entity.DeliveryAlbum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryAlbumRepository extends JpaRepository<DeliveryAlbum, Long> {

    Optional<DeliveryAlbum> findByBookingId(Long bookingId);

    @Query("SELECT a FROM DeliveryAlbum a LEFT JOIN FETCH a.photos WHERE a.booking.id = :bookingId")
    Optional<DeliveryAlbum> findByBookingIdWithPhotos(@Param("bookingId") Long bookingId);

    @Query("SELECT a FROM DeliveryAlbum a " +
           "LEFT JOIN FETCH a.photos " +
           "JOIN FETCH a.booking b " +
           "JOIN FETCH b.customer " +
           "JOIN FETCH b.photographerProfile p " +
           "JOIN FETCH p.user " +
           "WHERE a.booking.id = :bookingId")
    Optional<DeliveryAlbum> findByBookingIdWithDetails(@Param("bookingId") Long bookingId);
}
