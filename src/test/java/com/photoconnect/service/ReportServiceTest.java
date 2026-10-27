package com.photoconnect.service;

import com.photoconnect.dto.CreateReportRequest;
import com.photoconnect.dto.ReportDto;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.ReportCategory;
import com.photoconnect.entity.ReportStatus;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserReport;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.exception.UnauthorizedException;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.UserReportRepository;
import com.photoconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private UserReportRepository userReportRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private OperationalAuditService auditService;

    @InjectMocks
    private ReportServiceImpl reportService;

    private User customer;
    private User photographerUser;
    private PhotographerProfile photographerProfile;
    private Booking booking;

    @BeforeEach
    void setUp() {
        customer = new User();
        customer.setId(10L);
        customer.setFullName("Customer Alice");
        customer.setEmail("alice@example.com");
        customer.setRole(UserRole.CUSTOMER);
        customer.setStatus(UserStatus.ACTIVE);

        photographerUser = new User();
        photographerUser.setId(20L);
        photographerUser.setFullName("Photographer Bob");
        photographerUser.setEmail("bob@example.com");
        photographerUser.setRole(UserRole.PHOTOGRAPHER);
        photographerUser.setStatus(UserStatus.ACTIVE);

        photographerProfile = new PhotographerProfile();
        photographerProfile.setId(200L);
        photographerProfile.setUser(photographerUser);
        photographerProfile.setDisplayName("Bob's Photo Studio");

        booking = new Booking();
        booking.setId(1001L);
        booking.setCustomer(customer);
        booking.setPhotographerProfile(photographerProfile);
    }

    @Test
    void submitReport_successfulWithBooking_asCustomer() {
        CreateReportRequest request = new CreateReportRequest(
                ReportCategory.NO_SHOW,
                "Photographer did not arrive at the agreed studio location after 45 minutes of waiting.",
                1001L,
                null
        );

        when(userRepository.findById(10L)).thenReturn(Optional.of(customer));
        when(bookingRepository.findByIdWithDetails(1001L)).thenReturn(Optional.of(booking));
        when(userReportRepository.save(any(UserReport.class))).thenAnswer(invocation -> {
            UserReport rep = invocation.getArgument(0);
            rep.setId(1L);
            return rep;
        });

        ReportDto result = reportService.submitReport(10L, request);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.category()).isEqualTo(ReportCategory.NO_SHOW);
        assertThat(result.reporterName()).isEqualTo("Customer Alice");
        assertThat(result.reportedUserName()).isEqualTo("Photographer Bob");
        assertThat(result.status()).isEqualTo(ReportStatus.PENDING);
        verify(userReportRepository).save(any(UserReport.class));
    }

    @Test
    void submitReport_successfulWithBooking_asPhotographer() {
        CreateReportRequest request = new CreateReportRequest(
                ReportCategory.HARASSMENT,
                "Client was extremely verbally abusive in chat and demands unpaid services.",
                1001L,
                null
        );

        when(userRepository.findById(20L)).thenReturn(Optional.of(photographerUser));
        when(bookingRepository.findByIdWithDetails(1001L)).thenReturn(Optional.of(booking));
        when(userReportRepository.save(any(UserReport.class))).thenAnswer(invocation -> {
            UserReport rep = invocation.getArgument(0);
            rep.setId(2L);
            return rep;
        });

        ReportDto result = reportService.submitReport(20L, request);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.category()).isEqualTo(ReportCategory.HARASSMENT);
        assertThat(result.reporterName()).isEqualTo("Photographer Bob");
        assertThat(result.reportedUserName()).isEqualTo("Customer Alice");
        assertThat(result.status()).isEqualTo(ReportStatus.PENDING);
    }

    @Test
    void submitReport_unauthenticated_throwsUnauthorizedException() {
        CreateReportRequest request = new CreateReportRequest(ReportCategory.OTHER, "Test", null, null);

        assertThatThrownBy(() -> reportService.submitReport(null, request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("authenticated");
    }

    @Test
    void submitReport_strangerReportingBooking_throwsUnauthorizedException() {
        User stranger = new User();
        stranger.setId(99L);
        stranger.setStatus(UserStatus.ACTIVE);

        CreateReportRequest request = new CreateReportRequest(
                ReportCategory.SCAM_OR_FRAUD, "Suspicious booking", 1001L, null
        );

        when(userRepository.findById(99L)).thenReturn(Optional.of(stranger));
        when(bookingRepository.findByIdWithDetails(1001L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> reportService.submitReport(99L, request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("participant");
    }

    @Test
    void submitReport_missingCategory_throwsIllegalArgumentException() {
        CreateReportRequest request = new CreateReportRequest(null, "Details without category", 1001L, null);

        when(userRepository.findById(10L)).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> reportService.submitReport(10L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("category");
    }

    @Test
    void submitReport_missingDetails_throwsIllegalArgumentException() {
        CreateReportRequest request = new CreateReportRequest(ReportCategory.OTHER, "   ", 1001L, null);

        when(userRepository.findById(10L)).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> reportService.submitReport(10L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("details");
    }

    @Test
    void submitReport_oversizedDetails_throwsIllegalArgumentException() {
        CreateReportRequest request = new CreateReportRequest(
                ReportCategory.OTHER, "x".repeat(1501), 1001L, null
        );

        when(userRepository.findById(10L)).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> reportService.submitReport(10L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1500");
    }

    @Test
    void resolveReport_asAdmin_updatesStatusAndNotes() {
        User admin = new User();
        admin.setId(999L);
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        UserReport rep = new UserReport(customer, photographerUser, booking, ReportCategory.NO_SHOW, "Details");
        rep.setId(5L);
        rep.setStatus(ReportStatus.PENDING);

        when(userRepository.findById(999L)).thenReturn(Optional.of(admin));
        when(userReportRepository.findByIdWithDetails(5L)).thenReturn(Optional.of(rep));
        when(userReportRepository.save(any(UserReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReportDto result = reportService.resolveReport(5L, 999L, "Confirmed with studio log, deposit refunded.", ReportStatus.RESOLVED);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(result.adminNotes()).isEqualTo("Confirmed with studio log, deposit refunded.");
        assertThat(result.resolvedByAdminId()).isEqualTo(999L);
        assertThat(result.resolvedAt()).isNotNull();
        verify(auditService).record(999L, "REPORT_RESOLVED", "UserReport", 5L,
                com.photoconnect.entity.AuditOutcome.SUCCESS, "Administrator completed report moderation");
    }

    @Test
    void resolveReport_asManagerThroughLegacyAdminService_isDenied() {
        User manager = new User();
        manager.setId(77L);
        manager.setRole(UserRole.MANAGER);
        manager.setStatus(UserStatus.ACTIVE);
        when(userRepository.findById(77L)).thenReturn(Optional.of(manager));

        assertThatThrownBy(() -> reportService.resolveReport(5L, 77L, "bypass", ReportStatus.RESOLVED))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Administrator");
        verify(userReportRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void resolveReport_rejectsArbitraryTargetStatus() {
        User admin = new User();
        admin.setId(999L);
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        when(userRepository.findById(999L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> reportService.resolveReport(5L, 999L, "bypass", ReportStatus.PENDING))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RESOLVED or DISMISSED");
    }

    @Test
    void getReportsForAdmin_withFilter_returnsFilteredReports() {
        UserReport rep = new UserReport(customer, photographerUser, booking, ReportCategory.NO_SHOW, "Details");
        rep.setId(7L);
        rep.setStatus(ReportStatus.PENDING);

        when(userReportRepository.findByStatusWithDetails(ReportStatus.PENDING)).thenReturn(List.of(rep));

        List<ReportDto> list = reportService.getReportsForAdmin(ReportStatus.PENDING);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).id()).isEqualTo(7L);
    }

    @Test
    void countPendingReports_returnsCount() {
        when(userReportRepository.countByStatus(ReportStatus.PENDING)).thenReturn(3L);

        long count = reportService.countPendingReports();

        assertThat(count).isEqualTo(3L);
    }
}
