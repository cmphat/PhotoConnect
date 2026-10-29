package com.photoconnect.repository;

import com.photoconnect.entity.PlatformFee;
import com.photoconnect.entity.PlatformFeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlatformFeeRepository extends JpaRepository<PlatformFee, Long> {

    Optional<PlatformFee> findByBookingId(Long bookingId);

    boolean existsByBookingId(Long bookingId);

    @Query("SELECT f FROM PlatformFee f " +
           "JOIN FETCH f.booking b " +
           "JOIN FETCH f.photographerProfile p " +
           "WHERE f.photographerUser.id = :userId " +
           "ORDER BY f.createdAt DESC")
    List<PlatformFee> findByPhotographerUserIdWithDetails(@Param("userId") Long userId);

    @Query("SELECT f FROM PlatformFee f " +
           "JOIN FETCH f.booking b " +
           "JOIN FETCH f.photographerProfile p " +
           "JOIN FETCH f.photographerUser u " +
           "ORDER BY f.createdAt DESC")
    List<PlatformFee> findAllWithDetails();

    @Query("SELECT f FROM PlatformFee f " +
           "JOIN FETCH f.booking b " +
           "JOIN FETCH f.photographerProfile p " +
           "JOIN FETCH f.photographerUser u " +
           "WHERE f.status = :status " +
           "ORDER BY f.createdAt DESC")
    List<PlatformFee> findByStatusWithDetails(@Param("status") PlatformFeeStatus status);

    @Query("SELECT COALESCE(SUM(f.feeAmount), 0) FROM PlatformFee f WHERE f.status <> com.photoconnect.entity.PlatformFeeStatus.CANCELLED")
    BigDecimal sumTotalFeeAmount();

    @Query("SELECT COALESCE(SUM(f.paidAmount), 0) FROM PlatformFee f WHERE f.status <> com.photoconnect.entity.PlatformFeeStatus.CANCELLED")
    BigDecimal sumTotalPaidAmount();

    @Query("SELECT COALESCE(SUM(f.outstandingAmount), 0) FROM PlatformFee f WHERE f.status IN (com.photoconnect.entity.PlatformFeeStatus.UNPAID, com.photoconnect.entity.PlatformFeeStatus.PARTIALLY_PAID)")
    BigDecimal sumTotalOutstandingAmount();

    @Query("SELECT COUNT(DISTINCT f.photographerUser.id) FROM PlatformFee f WHERE f.status IN (com.photoconnect.entity.PlatformFeeStatus.UNPAID, com.photoconnect.entity.PlatformFeeStatus.PARTIALLY_PAID) AND f.outstandingAmount > 0")
    long countPhotographersWithOutstandingFee();

    @Query("SELECT COALESCE(SUM(f.feeAmount), 0) FROM PlatformFee f WHERE f.photographerUser.id = :userId AND f.status <> com.photoconnect.entity.PlatformFeeStatus.CANCELLED")
    BigDecimal sumFeeAmountByPhotographerUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(f.paidAmount), 0) FROM PlatformFee f WHERE f.photographerUser.id = :userId AND f.status <> com.photoconnect.entity.PlatformFeeStatus.CANCELLED")
    BigDecimal sumPaidAmountByPhotographerUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(f.outstandingAmount), 0) FROM PlatformFee f WHERE f.photographerUser.id = :userId AND f.status IN (com.photoconnect.entity.PlatformFeeStatus.UNPAID, com.photoconnect.entity.PlatformFeeStatus.PARTIALLY_PAID)")
    BigDecimal sumOutstandingAmountByPhotographerUserId(@Param("userId") Long userId);
}
