package com.photoconnect.repository;

import com.photoconnect.entity.SupportTicketMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SupportTicketMessageRepository extends JpaRepository<SupportTicketMessage, Long> {
    @Query("SELECT m FROM SupportTicketMessage m JOIN FETCH m.author WHERE m.ticket.id = :ticketId ORDER BY m.createdAt ASC")
    List<SupportTicketMessage> findByTicketIdWithAuthor(Long ticketId);
}
