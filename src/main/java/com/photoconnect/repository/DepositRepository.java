package com.photoconnect.repository;

import com.photoconnect.entity.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepositRepository extends JpaRepository<Deposit, Long> {

    @Query("""
            SELECT d FROM Deposit d
            JOIN FETCH d.booking b
            JOIN FETCH b.customer
            JOIN FETCH b.photographerProfile p
            JOIN FETCH p.user
            WHERE b.id = :bookingId
            """)
    Optional<Deposit> findByBookingIdWithDetails(@Param("bookingId") Long bookingId);

    boolean existsByBookingId(Long bookingId);

    long countByStatus(com.photoconnect.entity.DepositStatus status);

    @Query("""
            SELECT COALESCE(SUM(d.amount), 0)
            FROM Deposit d
            WHERE d.status = :status
            """)
    java.math.BigDecimal sumAmountByStatus(@Param("status") com.photoconnect.entity.DepositStatus status);
}
