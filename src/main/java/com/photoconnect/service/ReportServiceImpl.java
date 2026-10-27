package com.photoconnect.service;

import com.photoconnect.dto.CreateReportRequest;
import com.photoconnect.dto.ReportDto;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.ReportCategory;
import com.photoconnect.entity.ReportStatus;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserReport;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.entity.AuditOutcome;
import com.photoconnect.exception.InvalidBookingException;
import com.photoconnect.exception.UnauthorizedException;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.UserReportRepository;
import com.photoconnect.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReportServiceImpl implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);

    private final UserReportRepository userReportRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;
    private final OperationalAuditService auditService;

    @Autowired
    public ReportServiceImpl(UserReportRepository userReportRepository,
                             UserRepository userRepository,
                             BookingRepository bookingRepository,
                             @Autowired(required = false) NotificationService notificationService,
                             @Autowired(required = false) OperationalAuditService auditService) {
        this.userReportRepository = userReportRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    @Override
    public ReportDto submitReport(Long reporterUserId, CreateReportRequest request) {
        if (reporterUserId == null) {
            throw new UnauthorizedException("User must be authenticated to submit a report.");
        }
        if (request == null) {
            throw new IllegalArgumentException("Report request must not be null.");
        }

        User reporter = userRepository.findById(reporterUserId)
                .orElseThrow(() -> new UnauthorizedException("Reporter account not found."));

        if (reporter.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Only active accounts may submit dispute reports.");
        }

        if (request.getCategory() == null) {
            throw new IllegalArgumentException("Report category is required.");
        }

        if (request.getDetails() == null || request.getDetails().trim().isEmpty()) {
            throw new IllegalArgumentException("Report details description is required.");
        }

        String trimmedDetails = request.getDetails().trim();
        if (trimmedDetails.length() > 1500) {
            throw new IllegalArgumentException("Details cannot exceed 1500 characters.");
        }

        Booking booking = null;
        User reportedUser = null;

        if (request.getBookingId() != null) {
            booking = bookingRepository.findByIdWithDetails(request.getBookingId())
                    .orElseThrow(() -> new InvalidBookingException("Booking not found with ID: " + request.getBookingId()));

            Long customerId = booking.getCustomer() != null ? booking.getCustomer().getId() : null;
            Long photographerUserId = (booking.getPhotographerProfile() != null && booking.getPhotographerProfile().getUser() != null)
                    ? booking.getPhotographerProfile().getUser().getId()
                    : null;

            if (!reporterUserId.equals(customerId) && !reporterUserId.equals(photographerUserId)) {
                throw new UnauthorizedException("You can only report bookings that you are a participant of.");
            }

            // If reported user is not explicitly supplied, derive the counterparty
            if (request.getReportedUserId() == null) {
                if (reporterUserId.equals(customerId)) {
                    reportedUser = booking.getPhotographerProfile().getUser();
                } else {
                    reportedUser = booking.getCustomer();
                }
            }
        }

        if (reportedUser == null && request.getReportedUserId() != null) {
            reportedUser = userRepository.findById(request.getReportedUserId()).orElse(null);
        }

        UserReport report = new UserReport(reporter, reportedUser, booking, request.getCategory(), trimmedDetails);
        UserReport saved = userReportRepository.save(report);

        log.info("Trust & Safety report #{} created by user #{} (Category: {})", saved.getId(), reporterUserId, saved.getCategory());

        return ReportDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportDto> getReportsForAdmin(ReportStatus statusFilter) {
        List<UserReport> list;
        if (statusFilter != null) {
            list = userReportRepository.findByStatusWithDetails(statusFilter);
        } else {
            list = userReportRepository.findAllWithDetails();
        }
        return list.stream().map(ReportDto::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportDto> getReportsByReporter(Long reporterUserId) {
        if (reporterUserId == null) {
            return List.of();
        }
        return userReportRepository.findByReporterIdWithDetails(reporterUserId).stream()
                .map(ReportDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ReportDto resolveReport(Long reportId, Long adminUserId, String adminNotes, ReportStatus resolution) {
        if (reportId == null) {
            throw new IllegalArgumentException("Report ID is required.");
        }

        User actor = userRepository.findById(adminUserId)
                .orElseThrow(() -> new UnauthorizedException("Moderation actor not found."));
        if (actor.getStatus() != UserStatus.ACTIVE || actor.getRole() != com.photoconnect.entity.UserRole.ADMIN) {
            throw new UnauthorizedException("Administrator report moderation permission is required.");
        }

        ReportStatus targetStatus = resolution != null ? resolution : ReportStatus.RESOLVED;
        if (targetStatus != ReportStatus.RESOLVED && targetStatus != ReportStatus.DISMISSED) {
            throw new IllegalArgumentException("Reports may only be RESOLVED or DISMISSED through this operation.");
        }

        UserReport report = userReportRepository.findByIdWithDetails(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found with ID: " + reportId));
        if (report.getStatus() != ReportStatus.PENDING && report.getStatus() != ReportStatus.INVESTIGATING) {
            throw new IllegalStateException("Only pending or investigating reports can be resolved.");
        }
        report.setStatus(targetStatus);
        report.setAdminNotes(adminNotes != null ? adminNotes.trim() : null);
        report.setResolvedByAdminId(adminUserId);
        report.setResolvedAt(LocalDateTime.now());

        UserReport saved = userReportRepository.save(report);
        if (auditService != null) {
            auditService.record(adminUserId, "REPORT_" + targetStatus.name(), "UserReport", reportId,
                    AuditOutcome.SUCCESS, "Administrator completed report moderation");
        }
        log.info("Report #{} marked as {} by admin #{}", reportId, targetStatus, adminUserId);

        return ReportDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPendingReports() {
        return userReportRepository.countByStatus(ReportStatus.PENDING);
    }
}
