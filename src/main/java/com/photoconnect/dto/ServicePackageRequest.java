package com.photoconnect.dto;

import com.photoconnect.entity.PortfolioCategory;

import java.math.BigDecimal;

public class ServicePackageRequest {

    private String name;
    private String category;
    private String description;
    private BigDecimal price;
    private Integer durationMinutes;
    private Integer editedPhotoCount;
    private Integer deliveryDays;

    public ServicePackageRequest() {
    }

    public ServicePackageRequest(String name, String category, String description, BigDecimal price,
                                 Integer durationMinutes, Integer editedPhotoCount, Integer deliveryDays) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.price = price;
        this.durationMinutes = durationMinutes;
        this.editedPhotoCount = editedPhotoCount;
        this.deliveryDays = deliveryDays;
    }

    public void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Package name is required.");
        }
        if (name.trim().length() > 150) {
            throw new IllegalArgumentException("Package name cannot exceed 150 characters.");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Photography category is required.");
        }
        try {
            PortfolioCategory.fromFormValue(category);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid category: " + category);
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Package price must be greater than zero.");
        }
        if (price.compareTo(new BigDecimal("1000000000")) > 0) {
            throw new IllegalArgumentException("Package price exceeds maximum allowed value.");
        }
        if (durationMinutes == null || durationMinutes < 15 || durationMinutes > 1440) {
            throw new IllegalArgumentException("Duration must be between 15 minutes and 24 hours (1440 minutes).");
        }
        if (editedPhotoCount == null || editedPhotoCount < 0 || editedPhotoCount > 5000) {
            throw new IllegalArgumentException("Edited photo count must be between 0 and 5000.");
        }
        if (deliveryDays == null || deliveryDays < 1 || deliveryDays > 90) {
            throw new IllegalArgumentException("Delivery time must be between 1 and 90 days.");
        }
        if (description != null && description.trim().length() > 1000) {
            throw new IllegalArgumentException("Description cannot exceed 1000 characters.");
        }
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
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
}
