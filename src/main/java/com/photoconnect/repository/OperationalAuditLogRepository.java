package com.photoconnect.repository;

import com.photoconnect.entity.OperationalAuditLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OperationalAuditLogRepository extends JpaRepository<OperationalAuditLog, Long> {
    @Query("SELECT a FROM OperationalAuditLog a JOIN FETCH a.actor ORDER BY a.createdAt DESC")
    List<OperationalAuditLog> findRecent(Pageable pageable);

    @Query("SELECT a FROM OperationalAuditLog a JOIN FETCH a.actor WHERE a.actor.id = :actorId ORDER BY a.createdAt DESC")
    List<OperationalAuditLog> findByActorId(Long actorId, Pageable pageable);
}
