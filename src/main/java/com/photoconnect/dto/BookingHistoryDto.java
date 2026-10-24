package com.photoconnect.dto;

import com.photoconnect.entity.BookingHistory;
import java.time.LocalDateTime;

public class BookingHistoryDto {

    private Long id;
    private Long bookingId;
    private String oldStatus;
    private String newStatus;
    private String oldStatusDisplay;
    private String newStatusDisplay;
    private Long changedByUserId;
    private String changedByUserName;
    private LocalDateTime changedAt;
    private String reason;

    public BookingHistoryDto() {
    }

    public static BookingHistoryDto fromEntity(BookingHistory history) {
        BookingHistoryDto dto = new BookingHistoryDto();
        dto.setId(history.getId());
        dto.setBookingId(history.getBooking() != null ? history.getBooking().getId() : null);
        dto.setOldStatus(history.getOldStatus() != null ? history.getOldStatus().name() : null);
        dto.setNewStatus(history.getNewStatus() != null ? history.getNewStatus().name() : null);
        dto.setOldStatusDisplay(history.getOldStatus() != null ? history.getOldStatus().getDisplayName() : null);
        dto.setNewStatusDisplay(history.getNewStatus() != null ? history.getNewStatus().getDisplayName() : null);
        if (history.getChangedBy() != null) {
            dto.setChangedByUserId(history.getChangedBy().getId());
            dto.setChangedByUserName(history.getChangedBy().getFullName());
        }
        dto.setChangedAt(history.getChangedAt());
        dto.setReason(history.getReason());
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

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getOldStatusDisplay() {
        return oldStatusDisplay;
    }

    public void setOldStatusDisplay(String oldStatusDisplay) {
        this.oldStatusDisplay = oldStatusDisplay;
    }

    public String getNewStatusDisplay() {
        return newStatusDisplay;
    }

    public void setNewStatusDisplay(String newStatusDisplay) {
        this.newStatusDisplay = newStatusDisplay;
    }

    public Long getChangedByUserId() {
        return changedByUserId;
    }

    public void setChangedByUserId(Long changedByUserId) {
        this.changedByUserId = changedByUserId;
    }

    public String getChangedByUserName() {
        return changedByUserName;
    }

    public void setChangedByUserName(String changedByUserName) {
        this.changedByUserName = changedByUserName;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
