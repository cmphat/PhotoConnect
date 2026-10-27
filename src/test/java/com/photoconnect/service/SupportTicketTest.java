package com.photoconnect.service;

import com.photoconnect.entity.*;
import com.photoconnect.exception.UnauthorizedException;
import com.photoconnect.repository.SupportTicketMessageRepository;
import com.photoconnect.repository.SupportTicketRepository;
import com.photoconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportTicketTest {
    @Mock SupportTicketRepository tickets;
    @Mock SupportTicketMessageRepository messages;
    @Mock UserRepository users;
    @Mock NotificationService notifications;
    @Mock OperationalAuditService audit;
    SupportTicketService service;
    User customer;
    User manager;

    @BeforeEach
    void setUp() {
        service = new SupportTicketServiceImpl(tickets, messages, users, notifications, audit);
        customer = user(1L, UserRole.CUSTOMER);
        manager = user(2L, UserRole.MANAGER);
        lenient().when(users.findById(1L)).thenReturn(Optional.of(customer));
        lenient().when(users.findById(2L)).thenReturn(Optional.of(manager));
    }

    @Test
    void customerCreatesOwnTicket() {
        when(tickets.save(any())).thenAnswer(inv -> inv.getArgument(0));
        SupportTicket ticket = service.create(1L, SupportTicketCategory.BOOKING, "Need help", "Booking support needed");
        assertEquals(SupportTicketStatus.OPEN, ticket.getStatus());
        assertEquals(customer, ticket.getRequester());
    }

    @Test
    void unrelatedUserCannotReadTicket() {
        User other = user(3L, UserRole.CUSTOMER);
        when(users.findById(3L)).thenReturn(Optional.of(other));
        SupportTicket ticket = new SupportTicket(customer, SupportTicketCategory.OTHER, "Subject", "Description");
        when(tickets.findByIdWithUsers(10L)).thenReturn(Optional.of(ticket));
        assertThrows(UnauthorizedException.class, () -> service.getForRequester(10L, 3L));
    }

    @Test
    void managerMustAssignTicketBeforeReplyOrResolve() {
        SupportTicket ticket = new SupportTicket(customer, SupportTicketCategory.OTHER, "Subject", "Description");
        when(tickets.findByIdWithUsers(10L)).thenReturn(Optional.of(ticket));
        assertThrows(UnauthorizedException.class, () -> service.managerReply(10L, 2L, "Reply"));

        service.assignToSelf(10L, 2L);
        assertEquals(manager, ticket.getAssignedManager());
        assertEquals(SupportTicketStatus.IN_PROGRESS, ticket.getStatus());

        service.resolve(10L, 2L, "Resolved safely");
        assertEquals(SupportTicketStatus.RESOLVED, ticket.getStatus());
        assertNotNull(ticket.getResolvedAt());
        verify(notifications).sendNotification(eq(1L), eq(NotificationType.SUPPORT_UPDATE), anyString(), anyString(), eq(10L), eq("/support/10"));
    }

    @Test
    void managerCannotCreateRequesterTicket() {
        assertThrows(UnauthorizedException.class, () -> service.create(2L, SupportTicketCategory.OTHER, "Subject", "Description"));
    }

    private User user(Long id, UserRole role) {
        User u = new User(); u.setId(id); u.setRole(role); u.setStatus(UserStatus.ACTIVE); u.setFullName(role.name()); return u;
    }
}
