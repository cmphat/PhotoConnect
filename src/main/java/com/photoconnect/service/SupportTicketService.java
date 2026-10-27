package com.photoconnect.service;

import com.photoconnect.entity.*;
import java.util.List;

public interface SupportTicketService {
    SupportTicket create(Long requesterId, SupportTicketCategory category, String subject, String description);
    List<SupportTicket> listOwn(Long requesterId);
    SupportTicket getForRequester(Long ticketId, Long requesterId);
    List<SupportTicketMessage> messagesForRequester(Long ticketId, Long requesterId);
    void requesterReply(Long ticketId, Long requesterId, String message);
    void reopen(Long ticketId, Long requesterId, String message);
    List<SupportTicket> listForManager(Long managerId, SupportTicketStatus status);
    SupportTicket getForManager(Long ticketId, Long managerId);
    List<SupportTicketMessage> messagesForManager(Long ticketId, Long managerId);
    void assignToSelf(Long ticketId, Long managerId);
    void managerReply(Long ticketId, Long managerId, String message);
    void resolve(Long ticketId, Long managerId, String resolutionMessage);
}
