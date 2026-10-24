package com.photoconnect.dto;

import com.photoconnect.entity.ReportCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateReportRequest {

    @NotNull(message = "Report category is required")
    private ReportCategory category;

    @NotBlank(message = "Report details are required")
    @Size(max = 1500, message = "Details cannot exceed 1500 characters")
    private String details;

    private Long bookingId;
    private Long reportedUserId;

    public CreateReportRequest() {
    }

    public CreateReportRequest(ReportCategory category, String details, Long bookingId, Long reportedUserId) {
        this.category = category;
        this.details = details;
        this.bookingId = bookingId;
        this.reportedUserId = reportedUserId;
    }

    public ReportCategory getCategory() {
        return category;
    }

    public void setCategory(ReportCategory category) {
        this.category = category;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details != null ? details.trim() : null;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getReportedUserId() {
        return reportedUserId;
    }

    public void setReportedUserId(Long reportedUserId) {
        this.reportedUserId = reportedUserId;
    }
}
