package com.photoconnect.service;

import com.photoconnect.dto.ManagerDashboardStatsDto;
import com.photoconnect.entity.*;
import com.photoconnect.exception.UnauthorizedException;
import com.photoconnect.repository.*;
import com.photoconnect.security.PlatformPermission;
import com.photoconnect.security.RolePermissionPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

@Service
public class ManagerOperationsServiceImpl implements ManagerOperationsService {
    private final UserRepository userRepository;
    private final PhotographerProfileRepository photographerRepository;
    private final UserReportRepository reportRepository;
    private final BookingRepository bookingRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final AdminPhotographerService photographerService;
    private final AdminBookingService bookingService;
    private final NotificationService notificationService;
    private final OperationalAuditService auditService;

    public ManagerOperationsServiceImpl(UserRepository userRepository,
                                        PhotographerProfileRepository photographerRepository,
                                        UserReportRepository reportRepository,
                                        BookingRepository bookingRepository,
                                        SupportTicketRepository supportTicketRepository,
                                        AdminPhotographerService photographerService,
                                        AdminBookingService bookingService,
                                        NotificationService notificationService,
                                        OperationalAuditService auditService) {
        this.userRepository = userRepository;
        this.photographerRepository = photographerRepository;
        this.reportRepository = reportRepository;
        this.bookingRepository = bookingRepository;
        this.supportTicketRepository = supportTicketRepository;
        this.photographerService = photographerService;
        this.bookingService = bookingService;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    @Override
    @Transactional(readOnly = true)
    public ManagerDashboardStatsDto dashboard(Long managerId) {
        require(managerId, PlatformPermission.OPERATIONAL_ANALYTICS_VIEW);
        long reports = reportRepository.countByStatus(ReportStatus.PENDING)
                + reportRepository.countByStatus(ReportStatus.INVESTIGATING);
        long tickets = supportTicketRepository.countByStatusIn(EnumSet.of(
                SupportTicketStatus.OPEN, SupportTicketStatus.IN_PROGRESS, SupportTicketStatus.REOPENED));
        return new ManagerDashboardStatsDto(
                photographerRepository.countByVerificationStatus(PhotographerVerificationStatus.PENDING),
                reports,
                bookingRepository.countByStatus(BookingStatus.DISPUTED),
                tickets,
                bookingRepository.count());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperationalAuditLog> recentActivity(Long managerId, int limit) {
        require(managerId, PlatformPermission.AUDIT_LOG_VIEW);
        return auditService.recent(limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhotographerProfile> photographerApplications(Long managerId, PhotographerVerificationStatus status) {
        require(managerId, PlatformPermission.PHOTOGRAPHER_APPLICATION_REVIEW);
        return status == null ? photographerService.listAllPhotographers() : photographerService.listPhotographersByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public PhotographerProfile photographerApplication(Long managerId, Long profileId) {
        require(managerId, PlatformPermission.PHOTOGRAPHER_APPLICATION_REVIEW);
        return photographerService.getApplicationById(profileId);
    }

    @Override
    @Transactional
    public void approvePhotographer(Long managerId, Long profileId, String notes) {
        User manager = require(managerId, PlatformPermission.PHOTOGRAPHER_APPLICATION_REVIEW);
        try {
            PhotographerProfile profile = photographerService.approve(profileId, managerId, clean(notes, 1000));
            notificationService.sendNotification(profile.getUser().getId(), NotificationType.MODERATION_UPDATE,
                    "Hồ sơ nhiếp ảnh gia đã được duyệt", "Hồ sơ chuyên nghiệp của bạn đã được phê duyệt.", profileId, "/become-photographer/status");
        } catch (RuntimeException ex) {
            auditService.record(manager.getId(), "PHOTOGRAPHER_APPROVE", "PhotographerProfile", profileId, AuditOutcome.FAILED, "Approval rejected by domain validation");
            throw ex;
        }
    }

    @Override
    @Transactional
    public void rejectPhotographer(Long managerId, Long profileId, String reason, String notes) {
        User manager = require(managerId, PlatformPermission.PHOTOGRAPHER_APPLICATION_REVIEW);
        String rejection = required(reason, "Rejection reason", 1000);
        try {
            PhotographerProfile profile = photographerService.reject(profileId, managerId, rejection, clean(notes, 1000));
            notificationService.sendNotification(profile.getUser().getId(), NotificationType.MODERATION_UPDATE,
                    "Hồ sơ nhiếp ảnh gia cần cập nhật", "Hồ sơ chưa được duyệt. Vui lòng xem lý do và gửi lại.", profileId, "/become-photographer/status");
        } catch (RuntimeException ex) {
            auditService.record(manager.getId(), "PHOTOGRAPHER_REJECT", "PhotographerProfile", profileId, AuditOutcome.FAILED, "Rejection action failed domain validation");
            throw ex;
        }
    }

    @Override
    @Transactional
    public void suspendPhotographer(Long managerId, Long profileId, String reason, String notes) {
        User manager = require(managerId, PlatformPermission.PHOTOGRAPHER_APPLICATION_REVIEW);
        String suspension = required(reason, "Suspension reason", 1000);
        try {
            PhotographerProfile profile = photographerService.suspend(profileId, managerId, suspension, clean(notes, 1000));
            notificationService.sendNotification(profile.getUser().getId(), NotificationType.MODERATION_UPDATE,
                    "Tài khoản nhiếp ảnh gia tạm dừng", "Hồ sơ của bạn đã bị tạm ngưng hoạt động do vi phạm chính sách.", profileId, "/become-photographer/status");
        } catch (RuntimeException ex) {
            auditService.record(manager.getId(), "PHOTOGRAPHER_SUSPEND", "PhotographerProfile", profileId, AuditOutcome.FAILED, "Suspension action failed domain validation");
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserReport> reports(Long managerId, ReportStatus status) {
        require(managerId, PlatformPermission.USER_REPORT_REVIEW);
        return status == null ? reportRepository.findAllWithDetails() : reportRepository.findByStatusWithDetails(status);
    }

    @Override
    @Transactional(readOnly = true)
    public UserReport report(Long managerId, Long reportId) {
        require(managerId, PlatformPermission.USER_REPORT_REVIEW);
        return requireReport(reportId);
    }

    @Override
    @Transactional
    public void beginReportInvestigation(Long managerId, Long reportId, String notes) {
        User manager = require(managerId, PlatformPermission.USER_REPORT_REVIEW);
        UserReport report = requireReport(reportId);
        if (report.getStatus() != ReportStatus.PENDING) throw new IllegalStateException("Only PENDING reports can enter investigation.");
        report.setAssignedManager(manager);
        report.setInvestigationNotes(required(notes, "Investigation notes", 1500));
        report.setStatus(ReportStatus.INVESTIGATING);
        reportRepository.save(report);
        auditService.record(managerId, "REPORT_INVESTIGATE", "UserReport", reportId, AuditOutcome.SUCCESS, "Assigned report and began investigation");
    }

    @Override
    @Transactional
    public void resolveReport(Long managerId, Long reportId, ReportStatus target, String reason, String notes) {
        User manager = require(managerId, PlatformPermission.USER_REPORT_REVIEW);
        if (target != ReportStatus.RESOLVED && target != ReportStatus.DISMISSED) throw new IllegalArgumentException("Report outcome must be RESOLVED or DISMISSED.");
        UserReport report = requireReport(reportId);
        if (report.getStatus() != ReportStatus.INVESTIGATING) throw new IllegalStateException("Report must be UNDER REVIEW before resolution.");
        if (report.getAssignedManager() == null || !managerId.equals(report.getAssignedManager().getId())) {
            throw new UnauthorizedException("Only the assigned manager can resolve this report.");
        }
        String resolution = required(reason, "Resolution reason", 1000);
        report.setStatus(target);
        report.setResolutionReason(resolution);
        report.setAdminNotes(clean(notes, 1000));
        report.setResolvedByAdminId(managerId);
        report.setResolvedAt(LocalDateTime.now());
        reportRepository.save(report);
        notificationService.sendNotification(report.getReporter().getId(), NotificationType.MODERATION_UPDATE,
                "Cập nhật báo cáo", "Báo cáo #" + reportId + " đã được " + (target == ReportStatus.RESOLVED ? "xử lý" : "đóng") + ".",
                reportId, "/bookings");
        auditService.record(managerId, "REPORT_" + target.name(), "UserReport", reportId, AuditOutcome.SUCCESS, resolution);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> bookings(Long managerId, BookingStatus status) {
        require(managerId, PlatformPermission.BOOKING_OPERATIONAL_VIEW);
        return bookingService.listBookings(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Booking booking(Long managerId, Long bookingId) {
        require(managerId, PlatformPermission.BOOKING_OPERATIONAL_VIEW);
        return bookingService.getBookingDetails(bookingId);
    }

    @Override
    @Transactional
    public void resolveDispute(Long managerId, Long bookingId, String resolution, String notes) {
        require(managerId, PlatformPermission.BOOKING_DISPUTE_RESOLVE);
        bookingService.resolveDispute(bookingId, resolution, notes, managerId);
        auditService.record(managerId, "BOOKING_DISPUTE_RESOLVE", "Booking", bookingId,
                AuditOutcome.SUCCESS, "Resolution: " + clean(resolution, 40));
    }

    private UserReport requireReport(Long reportId) {
        return reportRepository.findByIdWithDetails(reportId).orElseThrow(() -> new IllegalArgumentException("Report not found."));
    }

    private User require(Long actorId, PlatformPermission permission) {
        User actor = userRepository.findById(actorId).orElseThrow(() -> new UnauthorizedException("Authenticated manager not found."));
        if (actor.getStatus() != UserStatus.ACTIVE || !RolePermissionPolicy.hasPermission(actor.getRole(), permission)) {
            throw new UnauthorizedException("Required operational permission is missing.");
        }
        return actor;
    }

    private String required(String value, String label, int max) {
        String result = clean(value, max);
        if (result == null || result.isBlank()) throw new IllegalArgumentException(label + " is required.");
        return result;
    }

    private String clean(String value, int max) {
        if (value == null) return null;
        String result = value.trim();
        if (result.length() > max) throw new IllegalArgumentException("Value exceeds " + max + " characters.");
        return result;
    }
}
