package com.photoconnect.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO representing the rule-based screening evaluation for a photographer application.
 * Evaluates 6 key criteria based strictly on declared professional info (total 100%):
 * 1. Mức độ đầy đủ thông tin hồ sơ (25%)
 * 2. Mô tả chuyên môn và dịch vụ (20%)
 * 3. Thông tin kinh nghiệm (15%)
 * 4. Thông tin thiết bị (15%)
 * 5. Khu vực hoạt động và giá dịch vụ (15%)
 * 6. Tài liệu nghề nghiệp tự nguyện cung cấp (10%)
 *
 * NOTE: Portfolio is NOT penalized as platform uploads require prior approval in V5.
 * NOTE: Sensitive attributes (age, gender, origin) are NEVER used.
 */
public class PhotographerEvaluationDto {

    private int totalScore;
    private String completenessLevel;
    private String badgeClass;
    private String recommendation;
    private String explanation;
    private List<CriterionItem> criteria = new ArrayList<>();
    private List<String> satisfiedItems = new ArrayList<>();
    private List<String> missingItems = new ArrayList<>();
    private List<String> manualVerificationPoints = new ArrayList<>();

    public PhotographerEvaluationDto() {}

    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }

    public String getCompletenessLevel() { return completenessLevel; }
    public void setCompletenessLevel(String completenessLevel) { this.completenessLevel = completenessLevel; }

    public String getBadgeClass() { return badgeClass; }
    public void setBadgeClass(String badgeClass) { this.badgeClass = badgeClass; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public List<CriterionItem> getCriteria() { return criteria; }
    public void setCriteria(List<CriterionItem> criteria) { this.criteria = criteria; }

    public List<String> getSatisfiedItems() { return satisfiedItems; }
    public void setSatisfiedItems(List<String> satisfiedItems) { this.satisfiedItems = satisfiedItems; }

    public List<String> getMissingItems() { return missingItems; }
    public void setMissingItems(List<String> missingItems) { this.missingItems = missingItems; }

    public List<String> getManualVerificationPoints() { return manualVerificationPoints; }
    public void setManualVerificationPoints(List<String> manualVerificationPoints) { this.manualVerificationPoints = manualVerificationPoints; }

    public static class CriterionItem {
        private String name;
        private int weight;
        private int earnedScore;
        private boolean met;
        private List<String> metDetails = new ArrayList<>();
        private List<String> missingDetails = new ArrayList<>();

        public CriterionItem(String name, int weight, int earnedScore, boolean met) {
            this.name = name;
            this.weight = weight;
            this.earnedScore = earnedScore;
            this.met = met;
        }

        public String getName() { return name; }
        public int getWeight() { return weight; }
        public int getEarnedScore() { return earnedScore; }
        public void setEarnedScore(int earnedScore) { this.earnedScore = earnedScore; }
        public boolean isMet() { return met; }
        public void setMet(boolean met) { this.met = met; }
        public List<String> getMetDetails() { return metDetails; }
        public void setMetDetails(List<String> metDetails) { this.metDetails = metDetails; }
        public List<String> getMissingDetails() { return missingDetails; }
        public void setMissingDetails(List<String> missingDetails) { this.missingDetails = missingDetails; }
    }
}
