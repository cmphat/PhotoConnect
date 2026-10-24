package com.photoconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class CalendarEventRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Start date and time are required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startAt;

    @NotNull(message = "End date and time are required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endAt;

    @Size(max = 50)
    private String eventType = "PERSONAL"; // PERSONAL, BLOCKED, AVAILABLE, SESSION

    @Size(max = 50)
    private String visibility = "PRIVATE"; // PRIVATE, PUBLIC

    public CalendarEventRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public String getEventType() {
        return eventType != null && !eventType.isBlank() ? eventType : "PERSONAL";
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getVisibility() {
        return visibility != null && !visibility.isBlank() ? visibility : "PRIVATE";
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public boolean isValidRange() {
        return startAt != null && endAt != null && endAt.isAfter(startAt);
    }
}
