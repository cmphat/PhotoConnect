package com.photoconnect.entity;

public enum ReportCategory {
    UNPROFESSIONAL_BEHAVIOR("Hành vi thiếu chuyên nghiệp", "Unprofessional Behavior"),
    NO_SHOW("Vắng mặt không báo trước", "No-Show / Absent"),
    HARASSMENT("Quấy rối hoặc ngôn từ thô lỗ", "Harassment / Inappropriate Language"),
    SCAM_OR_FRAUD("Lừa đảo hoặc gian lận thanh toán", "Scam or Fraud"),
    COPYRIGHT_INFRINGEMENT("Vi phạm bản quyền hình ảnh", "Copyright Infringement"),
    OTHER("Lý do khác", "Other");

    private final String vietnameseLabel;
    private final String englishLabel;

    ReportCategory(String vietnameseLabel, String englishLabel) {
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
