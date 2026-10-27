package com.photoconnect.repository;

import com.photoconnect.entity.SupportTicket;
import com.photoconnect.entity.SupportTicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    @Query("SELECT t FROM SupportTicket t JOIN FETCH t.requester LEFT JOIN FETCH t.assignedManager WHERE t.id = :id")
    Optional<SupportTicket> findByIdWithUsers(Long id);

    @Query("SELECT t FROM SupportTicket t JOIN FETCH t.requester LEFT JOIN FETCH t.assignedManager WHERE t.requester.id = :requesterId ORDER BY t.updatedAt DESC")
    List<SupportTicket> findByRequesterIdWithUsers(Long requesterId);

    @Query("SELECT t FROM SupportTicket t JOIN FETCH t.requester LEFT JOIN FETCH t.assignedManager WHERE (:status IS NULL OR t.status = :status) ORDER BY t.updatedAt DESC")
    List<SupportTicket> findForOperations(SupportTicketStatus status);

    long countByStatusIn(Collection<SupportTicketStatus> statuses);
}
