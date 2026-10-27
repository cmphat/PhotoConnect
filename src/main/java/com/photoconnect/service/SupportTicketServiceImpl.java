package com.photoconnect.service;

import com.photoconnect.entity.*;
import com.photoconnect.exception.UnauthorizedException;
import com.photoconnect.repository.SupportTicketMessageRepository;
import com.photoconnect.repository.SupportTicketRepository;
import com.photoconnect.repository.UserRepository;
import com.photoconnect.security.PlatformPermission;
import com.photoconnect.security.RolePermissionPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SupportTicketServiceImpl implements SupportTicketService {
    private final SupportTicketRepository ticketRepository;
    private final SupportTicketMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final OperationalAuditService auditService;

    public SupportTicketServiceImpl(SupportTicketRepository ticketRepository,
                                    SupportTicketMessageRepository messageRepository,
                                    UserRepository userRepository,
                                    NotificationService notificationService,
                                    OperationalAuditService auditService) {
        this.ticketRepository = ticketRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public SupportTicket create(Long requesterId, SupportTicketCategory category, String subject, String description) {
        User requester = requireActiveUser(requesterId);
        if (requester.getRole() != UserRole.CUSTOMER && requester.getRole() != UserRole.PHOTOGRAPHER) {
            throw new UnauthorizedException("Only customers and photographers can create support tickets.");
        }
        SupportTicket ticket = new SupportTicket(requester,
                category != null ? category : SupportTicketCategory.OTHER,
                required(subject, "Subject", 200), required(description, "Description", 2000));
        return ticketRepository.save(ticket);
    }

    @Override @Transactional(readOnly = true)
    public List<SupportTicket> listOwn(Long requesterId) { requireActiveUser(requesterId); return ticketRepository.findByRequesterIdWithUsers(requesterId); }

    @Override @Transactional(readOnly = true)
    public SupportTicket getForRequester(Long ticketId, Long requesterId) { return requireOwned(ticketId, requesterId); }

    @Override @Transactional(readOnly = true)
    public List<SupportTicketMessage> messagesForRequester(Long ticketId, Long requesterId) {
        requireOwned(ticketId, requesterId); return messageRepository.findByTicketIdWithAuthor(ticketId);
    }

    @Override
    @Transactional
    public void requesterReply(Long ticketId, Long requesterId, String message) {
        SupportTicket ticket = requireOwned(ticketId, requesterId);
        if (ticket.getStatus() == SupportTicketStatus.RESOLVED) throw new IllegalStateException("Reopen the ticket before replying.");
        messageRepository.save(new SupportTicketMessage(ticket, ticket.getRequester(), required(message, "Message", 2000)));
    }

    @Override
    @Transactional
    public void reopen(Long ticketId, Long requesterId, String message) {
        SupportTicket ticket = requireOwned(ticketId, requesterId);
        if (ticket.getStatus() != SupportTicketStatus.RESOLVED) throw new IllegalStateException("Only resolved tickets can be reopened.");
        ticket.setStatus(SupportTicketStatus.REOPENED);
        ticket.setResolvedAt(null);
        ticketRepository.save(ticket);
        messageRepository.save(new SupportTicketMessage(ticket, ticket.getRequester(), required(message, "Reason", 2000)));
    }

    @Override @Transactional(readOnly = true)
    public List<SupportTicket> listForManager(Long managerId, SupportTicketStatus status) {
        requireManager(managerId); return ticketRepository.findForOperations(status);
    }

    @Override @Transactional(readOnly = true)
    public SupportTicket getForManager(Long ticketId, Long managerId) { requireManager(managerId); return requireTicket(ticketId); }

    @Override @Transactional(readOnly = true)
    public List<SupportTicketMessage> messagesForManager(Long ticketId, Long managerId) {
        requireManager(managerId); requireTicket(ticketId); return messageRepository.findByTicketIdWithAuthor(ticketId);
    }

    @Override
    @Transactional
    public void assignToSelf(Long ticketId, Long managerId) {
        User manager = requireManager(managerId);
        SupportTicket ticket = requireTicket(ticketId);
        if (ticket.getStatus() == SupportTicketStatus.RESOLVED) throw new IllegalStateException("Resolved tickets cannot be assigned.");
        if (ticket.getAssignedManager() != null && !managerId.equals(ticket.getAssignedManager().getId())) {
            throw new IllegalStateException("Ticket is already assigned to another manager.");
        }
        ticket.setAssignedManager(manager);
        ticket.setStatus(SupportTicketStatus.IN_PROGRESS);
        ticketRepository.save(ticket);
        auditService.record(managerId, "SUPPORT_TICKET_ASSIGN", "SupportTicket", ticketId, AuditOutcome.SUCCESS, "Assigned support ticket to self");
    }

    @Override
    @Transactional
    public void managerReply(Long ticketId, Long managerId, String message) {
        User manager = requireManager(managerId);
        SupportTicket ticket = requireAssigned(ticketId, managerId);
        if (ticket.getStatus() == SupportTicketStatus.RESOLVED) throw new IllegalStateException("Resolved tickets cannot receive manager replies.");
        messageRepository.save(new SupportTicketMessage(ticket, manager, required(message, "Message", 2000)));
        notificationService.sendNotification(ticket.getRequester().getId(), NotificationType.SUPPORT_UPDATE,
                "Cập nhật yêu cầu hỗ trợ", "Yêu cầu #" + ticketId + " có phản hồi mới.", ticketId, "/support/" + ticketId);
        auditService.record(managerId, "SUPPORT_TICKET_REPLY", "SupportTicket", ticketId, AuditOutcome.SUCCESS, "Responded to support ticket");
    }

    @Override
    @Transactional
    public void resolve(Long ticketId, Long managerId, String resolutionMessage) {
        User manager = requireManager(managerId);
        SupportTicket ticket = requireAssigned(ticketId, managerId);
        if (ticket.getStatus() == SupportTicketStatus.RESOLVED) throw new IllegalStateException("Ticket is already resolved.");
        String message = required(resolutionMessage, "Resolution", 2000);
        messageRepository.save(new SupportTicketMessage(ticket, manager, message));
        ticket.setStatus(SupportTicketStatus.RESOLVED);
        ticket.setResolvedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        notificationService.sendNotification(ticket.getRequester().getId(), NotificationType.SUPPORT_UPDATE,
                "Yêu cầu hỗ trợ đã xử lý", "Yêu cầu #" + ticketId + " đã được giải quyết.", ticketId, "/support/" + ticketId);
        auditService.record(managerId, "SUPPORT_TICKET_RESOLVE", "SupportTicket", ticketId, AuditOutcome.SUCCESS, "Resolved support ticket");
    }

    private SupportTicket requireOwned(Long ticketId, Long requesterId) {
        requireActiveUser(requesterId);
        SupportTicket ticket = requireTicket(ticketId);
        if (!requesterId.equals(ticket.getRequester().getId())) throw new UnauthorizedException("You cannot access another user's support ticket.");
        return ticket;
    }

    private SupportTicket requireAssigned(Long ticketId, Long managerId) {
        SupportTicket ticket = requireTicket(ticketId);
        if (ticket.getAssignedManager() == null || !managerId.equals(ticket.getAssignedManager().getId())) {
            throw new UnauthorizedException("Assign the ticket to yourself before changing it.");
        }
        return ticket;
    }

    private SupportTicket requireTicket(Long id) { return ticketRepository.findByIdWithUsers(id).orElseThrow(() -> new IllegalArgumentException("Support ticket not found.")); }

    private User requireActiveUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UnauthorizedException("Authenticated user not found."));
        if (user.getStatus() != UserStatus.ACTIVE) throw new UnauthorizedException("Active account required.");
        return user;
    }

    private User requireManager(Long id) {
        User user = requireActiveUser(id);
        if (!RolePermissionPolicy.hasPermission(user.getRole(), PlatformPermission.SUPPORT_TICKET_MANAGE)) {
            throw new UnauthorizedException("Manager permission required.");
        }
        return user;
    }

    private String required(String value, String field, int max) {
        String clean = value != null ? value.trim() : "";
        if (clean.isEmpty()) throw new IllegalArgumentException(field + " is required.");
        if (clean.length() > max) throw new IllegalArgumentException(field + " is too long.");
        return clean;
    }
}
