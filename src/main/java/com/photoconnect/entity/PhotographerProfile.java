package com.photoconnect.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "photographer_profiles")
public class PhotographerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // A User can have at most one PhotographerProfile.
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String bio;

    @Column(length = 100)
    private String city;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "price_from", precision = 18, scale = 2)
    private BigDecimal priceFrom;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 30)
    private PhotographerVerificationStatus verificationStatus = PhotographerVerificationStatus.PENDING;

    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public PhotographerProfile() {
    }

    public PhotographerProfile(User user, String displayName, String bio, String city, Integer experienceYears, BigDecimal priceFrom, PhotographerVerificationStatus verificationStatus) {
        this.user = user;
        this.displayName = displayName;
        this.bio = bio;
        this.city = city;
        this.experienceYears = experienceYears;
        this.priceFrom = priceFrom;
        this.verificationStatus = verificationStatus;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public BigDecimal getPriceFrom() {
        return priceFrom;
    }

    public void setPriceFrom(BigDecimal priceFrom) {
        this.priceFrom = priceFrom;
    }

    public PhotographerVerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(PhotographerVerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
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
