package com.photoconnect.repository;

import com.photoconnect.entity.ReportStatus;
import com.photoconnect.entity.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserReportRepository extends JpaRepository<UserReport, Long> {

    @Query("SELECT r FROM UserReport r JOIN FETCH r.reporter LEFT JOIN FETCH r.reportedUser LEFT JOIN FETCH r.booking WHERE r.id = :id")
    Optional<UserReport> findByIdWithDetails(Long id);

    @Query("SELECT r FROM UserReport r JOIN FETCH r.reporter LEFT JOIN FETCH r.reportedUser LEFT JOIN FETCH r.booking ORDER BY r.createdAt DESC")
    List<UserReport> findAllWithDetails();

    @Query("SELECT r FROM UserReport r JOIN FETCH r.reporter LEFT JOIN FETCH r.reportedUser LEFT JOIN FETCH r.booking WHERE r.status = :status ORDER BY r.createdAt DESC")
    List<UserReport> findByStatusWithDetails(ReportStatus status);

    @Query("SELECT r FROM UserReport r JOIN FETCH r.reporter LEFT JOIN FETCH r.reportedUser LEFT JOIN FETCH r.booking WHERE r.reporter.id = :reporterId ORDER BY r.createdAt DESC")
    List<UserReport> findByReporterIdWithDetails(Long reporterId);

    @Query("SELECT r FROM UserReport r JOIN FETCH r.reporter LEFT JOIN FETCH r.reportedUser LEFT JOIN FETCH r.booking WHERE r.booking.id = :bookingId ORDER BY r.createdAt DESC")
    List<UserReport> findByBookingIdWithDetails(Long bookingId);

    long countByStatus(ReportStatus status);
}
