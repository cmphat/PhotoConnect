package com.photoconnect.dto;

import com.photoconnect.entity.ReportCategory;
import com.photoconnect.entity.ReportStatus;
import com.photoconnect.entity.UserReport;

import java.time.LocalDateTime;

public record ReportDto(
        Long id,
        Long reporterId,
        String reporterName,
        String reporterEmail,
        Long reportedUserId,
        String reportedUserName,
        Long bookingId,
        ReportCategory category,
        String categoryDisplayName,
        String details,
        ReportStatus status,
        String statusDisplayName,
        String adminNotes,
        Long resolvedByAdminId,
        LocalDateTime resolvedAt,
        LocalDateTime createdAt
) {
    public static ReportDto fromEntity(UserReport report) {
        if (report == null) {
            return null;
        }

        String reporterName = report.getReporter() != null ? report.getReporter().getFullName() : "Anonymous";
        String reporterEmail = report.getReporter() != null ? report.getReporter().getEmail() : "";
        Long reportedUserId = report.getReportedUser() != null ? report.getReportedUser().getId() : null;
        String reportedUserName = report.getReportedUser() != null ? report.getReportedUser().getFullName() : null;
        Long bookingId = report.getBooking() != null ? report.getBooking().getId() : null;

        return new ReportDto(
                report.getId(),
                report.getReporter() != null ? report.getReporter().getId() : null,
                reporterName,
                reporterEmail,
                reportedUserId,
                reportedUserName,
                bookingId,
                report.getCategory(),
                report.getCategory() != null ? report.getCategory().getDisplayName() : "General",
                report.getDetails(),
                report.getStatus(),
                report.getStatus() != null ? report.getStatus().getDisplayName() : "Pending",
                report.getAdminNotes(),
                report.getResolvedByAdminId(),
                report.getResolvedAt(),
                report.getCreatedAt()
        );
    }
}
