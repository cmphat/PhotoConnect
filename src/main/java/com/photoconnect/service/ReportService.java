package com.photoconnect.service;

import com.photoconnect.dto.CreateReportRequest;
import com.photoconnect.dto.ReportDto;
import com.photoconnect.entity.ReportStatus;

import java.util.List;

public interface ReportService {

    ReportDto submitReport(Long reporterUserId, CreateReportRequest request);

    List<ReportDto> getReportsForAdmin(ReportStatus statusFilter);

    List<ReportDto> getReportsByReporter(Long reporterUserId);

    ReportDto resolveReport(Long reportId, Long adminUserId, String adminNotes, ReportStatus resolution);

    long countPendingReports();
}
