package com.photoconnect.dto;

import com.photoconnect.entity.RescheduleRequest;
import com.photoconnect.entity.RescheduleStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class RescheduleRequestDto {

    private Long id;
    private Long bookingId;
    private Long requestedByUserId;
    private String requestedByName;
    private LocalDate proposedDate;
    private LocalTime proposedTime;
    private String reason;
    private RescheduleStatus status;
    private String responseNote;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public RescheduleRequestDto() {
    }

    public static RescheduleRequestDto from(RescheduleRequest request) {
        if (request == null) return null;
        RescheduleRequestDto dto = new RescheduleRequestDto();
        dto.setId(request.getId());
        dto.setBookingId(request.getBooking().getId());
        dto.setRequestedByUserId(request.getRequestedBy().getId());
        dto.setRequestedByName(request.getRequestedBy().getFullName());
        dto.setProposedDate(request.getProposedDate());
        dto.setProposedTime(request.getProposedTime());
        dto.setReason(request.getReason());
        dto.setStatus(request.getStatus());
        dto.setResponseNote(request.getResponseNote());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setResolvedAt(request.getResolvedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getRequestedByUserId() {
        return requestedByUserId;
    }

    public void setRequestedByUserId(Long requestedByUserId) {
        this.requestedByUserId = requestedByUserId;
    }

    public String getRequestedByName() {
        return requestedByName;
    }

    public void setRequestedByName(String requestedByName) {
        this.requestedByName = requestedByName;
    }

    public LocalDate getProposedDate() {
        return proposedDate;
    }

    public void setProposedDate(LocalDate proposedDate) {
        this.proposedDate = proposedDate;
    }

    public LocalTime getProposedTime() {
        return proposedTime;
    }

    public void setProposedTime(LocalTime proposedTime) {
        this.proposedTime = proposedTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public RescheduleStatus getStatus() {
        return status;
    }

    public void setStatus(RescheduleStatus status) {
        this.status = status;
    }

    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : "";
    }

    public String getBadgeClass() {
        return status != null ? status.getBadgeClass() : "pc-badge-secondary";
    }

    public String getResponseNote() {
        return responseNote;
    }

    public void setResponseNote(String responseNote) {
        this.responseNote = responseNote;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
