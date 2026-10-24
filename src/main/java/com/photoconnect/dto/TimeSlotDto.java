package com.photoconnect.dto;

import java.time.LocalTime;

public class TimeSlotDto {

    private LocalTime startTime;
    private LocalTime endTime;
    private boolean available;
    private String reason;

    public TimeSlotDto() {
    }

    public TimeSlotDto(LocalTime startTime, LocalTime endTime, boolean available, String reason) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.available = available;
        this.reason = reason;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
