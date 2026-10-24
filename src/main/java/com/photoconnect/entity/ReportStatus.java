package com.photoconnect.entity;

public enum ReportStatus {
    PENDING("Chờ xử lý", "Pending Review"),
    INVESTIGATING("Đang xác minh", "Investigating"),
    RESOLVED("Đã xử lý", "Resolved"),
    DISMISSED("Đã từ chối / Bỏ qua", "Dismissed");

    private final String vietnameseLabel;
    private final String englishLabel;

    ReportStatus(String vietnameseLabel, String englishLabel) {
        this.vietnameseLabel = vietnameseLabel;
        this.englishLabel = englishLabel;
    }

    public String getVietnameseLabel() {
        return vietnameseLabel;
    }

    public String getEnglishLabel() {
        return englishLabel;
    }

    public String getDisplayName() {
        return vietnameseLabel + " (" + englishLabel + ")";
    }
}
