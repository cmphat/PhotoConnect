package com.photoconnect.dto;

import com.photoconnect.entity.PortfolioCategory;
import com.photoconnect.entity.ServicePackage;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Locale;

public class ServicePackageDto {

    private Long id;
    private Long photographerProfileId;
    private String photographerName;
    private PortfolioCategory category;
    private String categoryDisplayName;
    private String name;
    private String description;
    private BigDecimal price;
    private String formattedPrice;
    private Integer durationMinutes;
    private String formattedDuration;
    private Integer editedPhotoCount;
    private Integer deliveryDays;
    private BigDecimal nightSurchargeRate;
    private Integer includedTravelKm;
    private BigDecimal excessTravelRate;
    private BigDecimal additionalHourFee;
    private BigDecimal extraEditedPhotoFee;
    private BigDecimal assistantFee;
    private boolean active;
    private boolean recommended;
    private LocalDateTime createdAt;

    public ServicePackageDto() {
    }

    public static ServicePackageDto fromEntity(ServicePackage entity) {
        if (entity == null) {
            return null;
        }
        ServicePackageDto dto = new ServicePackageDto();
        dto.setId(entity.getId());
        if (entity.getPhotographerProfile() != null) {
            dto.setPhotographerProfileId(entity.getPhotographerProfile().getId());
            dto.setPhotographerName(entity.getPhotographerProfile().getDisplayName());
        }
        dto.setCategory(entity.getCategory());
        dto.setCategoryDisplayName(entity.getCategory() != null ? entity.getCategory().getDisplayName() : "General");
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setPrice(entity.getPrice());
        if (entity.getPrice() != null) {
            NumberFormat vndFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
            dto.setFormattedPrice(vndFormat.format(entity.getPrice()));
        }
        dto.setDurationMinutes(entity.getDurationMinutes());
        if (entity.getDurationMinutes() != null) {
            int hours = entity.getDurationMinutes() / 60;
            int mins = entity.getDurationMinutes() % 60;
            if (hours > 0 && mins > 0) {
                dto.setFormattedDuration(hours + "h " + mins + "m");
            } else if (hours > 0) {
                dto.setFormattedDuration(hours + (hours > 1 ? " hours" : " hour"));
            } else {
                dto.setFormattedDuration(mins + " mins");
            }
        }
        dto.setEditedPhotoCount(entity.getEditedPhotoCount());
        dto.setDeliveryDays(entity.getDeliveryDays());
        dto.setNightSurchargeRate(entity.getNightSurchargeRate());
        dto.setIncludedTravelKm(entity.getIncludedTravelKm());
        dto.setExcessTravelRate(entity.getExcessTravelRate());
        dto.setAdditionalHourFee(entity.getAdditionalHourFee());
        dto.setExtraEditedPhotoFee(entity.getExtraEditedPhotoFee());
        dto.setAssistantFee(entity.getAssistantFee());
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPhotographerProfileId() {
        return photographerProfileId;
    }

    public void setPhotographerProfileId(Long photographerProfileId) {
        this.photographerProfileId = photographerProfileId;
    }

    public String getPhotographerName() {
        return photographerName;
    }

    public void setPhotographerName(String photographerName) {
        this.photographerName = photographerName;
    }

    public PortfolioCategory getCategory() {
        return category;
    }

    public void setCategory(PortfolioCategory category) {
        this.category = category;
    }

    public String getCategoryDisplayName() {
        return categoryDisplayName;
    }

    public void setCategoryDisplayName(String categoryDisplayName) {
        this.categoryDisplayName = categoryDisplayName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getFormattedPrice() {
        return formattedPrice;
    }

    public void setFormattedPrice(String formattedPrice) {
        this.formattedPrice = formattedPrice;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getFormattedDuration() {
        return formattedDuration;
    }

    public void setFormattedDuration(String formattedDuration) {
        this.formattedDuration = formattedDuration;
    }

    public Integer getEditedPhotoCount() {
        return editedPhotoCount;
    }

    public void setEditedPhotoCount(Integer editedPhotoCount) {
        this.editedPhotoCount = editedPhotoCount;
    }

    public Integer getDeliveryDays() {
        return deliveryDays;
    }

    public void setDeliveryDays(Integer deliveryDays) {
        this.deliveryDays = deliveryDays;
    }

    public BigDecimal getNightSurchargeRate() { return nightSurchargeRate; }
    public void setNightSurchargeRate(BigDecimal value) { this.nightSurchargeRate = value; }
    public Integer getIncludedTravelKm() { return includedTravelKm; }
    public void setIncludedTravelKm(Integer value) { this.includedTravelKm = value; }
    public BigDecimal getExcessTravelRate() { return excessTravelRate; }
    public void setExcessTravelRate(BigDecimal value) { this.excessTravelRate = value; }
    public BigDecimal getAdditionalHourFee() { return additionalHourFee; }
    public void setAdditionalHourFee(BigDecimal value) { this.additionalHourFee = value; }
    public BigDecimal getExtraEditedPhotoFee() { return extraEditedPhotoFee; }
    public void setExtraEditedPhotoFee(BigDecimal value) { this.extraEditedPhotoFee = value; }
    public BigDecimal getAssistantFee() { return assistantFee; }
    public void setAssistantFee(BigDecimal value) { this.assistantFee = value; }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isRecommended() {
        return recommended;
    }

    public void setRecommended(boolean recommended) {
        this.recommended = recommended;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
