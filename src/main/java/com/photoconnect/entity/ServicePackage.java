package com.photoconnect.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a structured photography service package offered by a photographer.
 *
 * Each package defines pricing, photography duration, expected deliverables
 * (edited photo count, delivery timeframe in days), and associated specialty category.
 */
@Entity
@Table(name = "service_packages", indexes = {
        @Index(name = "idx_service_packages_photographer", columnList = "photographer_profile_id"),
        @Index(name = "idx_service_packages_category", columnList = "category"),
        @Index(name = "idx_service_packages_active", columnList = "active")
})
public class ServicePackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "photographer_profile_id", nullable = false)
    private PhotographerProfile photographerProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PortfolioCategory category;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "edited_photo_count", nullable = false)
    private Integer editedPhotoCount;

    @Column(name = "delivery_days", nullable = false)
    private Integer deliveryDays;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ServicePackage() {
    }

    public ServicePackage(PhotographerProfile photographerProfile,
                          PortfolioCategory category,
                          String name,
                          String description,
                          BigDecimal price,
                          Integer durationMinutes,
                          Integer editedPhotoCount,
                          Integer deliveryDays) {
        this.photographerProfile = photographerProfile;
        this.category = category;
        this.name = name;
        this.description = description;
        this.price = price;
        this.durationMinutes = durationMinutes;
        this.editedPhotoCount = editedPhotoCount;
        this.deliveryDays = deliveryDays;
        this.active = true;
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PhotographerProfile getPhotographerProfile() {
        return photographerProfile;
    }

    public void setPhotographerProfile(PhotographerProfile photographerProfile) {
        this.photographerProfile = photographerProfile;
    }

    public PortfolioCategory getCategory() {
        return category;
    }

    public void setCategory(PortfolioCategory category) {
        this.category = category;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
