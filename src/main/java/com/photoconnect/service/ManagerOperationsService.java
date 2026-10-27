package com.photoconnect.service;

import com.photoconnect.dto.ManagerDashboardStatsDto;
import com.photoconnect.entity.*;
import java.util.List;

public interface ManagerOperationsService {
    ManagerDashboardStatsDto dashboard(Long managerId);
    List<OperationalAuditLog> recentActivity(Long managerId, int limit);
    List<PhotographerProfile> photographerApplications(Long managerId, PhotographerVerificationStatus status);
    PhotographerProfile photographerApplication(Long managerId, Long profileId);
    void approvePhotographer(Long managerId, Long profileId, String notes);
    void rejectPhotographer(Long managerId, Long profileId, String reason, String notes);
    void suspendPhotographer(Long managerId, Long profileId, String reason, String notes);
    List<UserReport> reports(Long managerId, ReportStatus status);
    UserReport report(Long managerId, Long reportId);
    void beginReportInvestigation(Long managerId, Long reportId, String notes);
    void resolveReport(Long managerId, Long reportId, ReportStatus target, String reason, String notes);
    List<Booking> bookings(Long managerId, BookingStatus status);
    Booking booking(Long managerId, Long bookingId);
    void resolveDispute(Long managerId, Long bookingId, String resolution, String notes);
}
