package com.photoconnect.service;

import com.photoconnect.entity.*;
import com.photoconnect.exception.UnauthorizedException;
import com.photoconnect.repository.*;
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
class ManagerServiceTest {
    @Mock UserRepository users;
    @Mock PhotographerProfileRepository photographers;
    @Mock UserReportRepository reports;
    @Mock BookingRepository bookings;
    @Mock SupportTicketRepository tickets;
    @Mock AdminPhotographerService photographerService;
    @Mock AdminBookingService bookingService;
    @Mock NotificationService notifications;
    @Mock OperationalAuditService audit;
    ManagerOperationsService service;
    User manager;

    @BeforeEach
    void setUp() {
        service = new ManagerOperationsServiceImpl(users, photographers, reports, bookings, tickets,
                photographerService, bookingService, notifications, audit);
        manager = user(50L, UserRole.MANAGER);
        lenient().when(users.findById(50L)).thenReturn(Optional.of(manager));
    }

    @Test
    void customerCannotUseManagerOperationsAtServiceLayer() {
        User customer = user(1L, UserRole.CUSTOMER);
        when(users.findById(1L)).thenReturn(Optional.of(customer));
        assertThrows(UnauthorizedException.class, () -> service.dashboard(1L));
        verifyNoInteractions(photographers, reports, bookings, tickets);
    }

    @Test
    void reportRequiresAssignmentAndValidLifecycle() {
        User reporter = user(1L, UserRole.CUSTOMER);
        UserReport report = new UserReport(reporter, null, null, ReportCategory.OTHER, "Details");
        report.setId(9L);
        when(reports.findByIdWithDetails(9L)).thenReturn(Optional.of(report));

        assertThrows(IllegalStateException.class,
                () -> service.resolveReport(50L, 9L, ReportStatus.RESOLVED, "Done", "Notes"));

        service.beginReportInvestigation(50L, 9L, "Evidence review started");
        assertEquals(ReportStatus.INVESTIGATING, report.getStatus());
        assertEquals(manager, report.getAssignedManager());

        service.resolveReport(50L, 9L, ReportStatus.RESOLVED, "Policy violation confirmed", "Safe note");
        assertEquals(ReportStatus.RESOLVED, report.getStatus());
        assertEquals("Policy violation confirmed", report.getResolutionReason());
        verify(notifications).sendNotification(eq(1L), eq(NotificationType.MODERATION_UPDATE), anyString(), anyString(), eq(9L), anyString());
    }

    @Test
    void managerCannotUseReportResolutionToSetArbitraryStatus() {
        assertThrows(IllegalArgumentException.class,
                () -> service.resolveReport(50L, 9L, ReportStatus.PENDING, "reason", "notes"));
    }

    @Test
    void disputeDelegatesOnlyAfterManagerPermissionCheck() {
        service.resolveDispute(50L, 7L, "PAY_PHOTOGRAPHER", "Evidence reviewed");
        verify(bookingService).resolveDispute(7L, "PAY_PHOTOGRAPHER", "Evidence reviewed", 50L);
        verify(audit).record(eq(50L), eq("BOOKING_DISPUTE_RESOLVE"), eq("Booking"), eq(7L), eq(AuditOutcome.SUCCESS), anyString());
    }

    @Test
    void managerCanSuspendPhotographerWithAuditAndNotification() {
        PhotographerProfile profile = new PhotographerProfile();
        profile.setId(10L);
        profile.setUser(user(22L, UserRole.PHOTOGRAPHER));
        when(photographerService.suspend(10L, 50L, "Conduct violation", "Internal notes")).thenReturn(profile);

        service.suspendPhotographer(50L, 10L, "Conduct violation", "Internal notes");

        verify(photographerService).suspend(10L, 50L, "Conduct violation", "Internal notes");
        verify(notifications).sendNotification(eq(22L), eq(NotificationType.MODERATION_UPDATE), anyString(), anyString(), eq(10L), anyString());
    }


    private User user(Long id, UserRole role) {
        User user = new User(); user.setId(id); user.setRole(role); user.setStatus(UserStatus.ACTIVE); user.setFullName(role.name()); return user;
    }
}
