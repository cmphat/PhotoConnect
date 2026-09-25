package com.photoconnect.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    @Column(length = 100)
    private String country;

    @Column(length = 255)
    private String headline;

    @Column(length = 255)
    private String specialties;

    @Column(name = "website_url", length = 255)
    private String websiteUrl;

    @Column(name = "instagram_url", length = 255)
    private String instagramUrl;

    @Column(name = "facebook_url", length = 255)
    private String facebookUrl;

    @Column(name = "equipment_summary", length = 500)
    private String equipmentSummary;

    @Column(length = 150)
    private String languages;

    @Column(name = "travel_available", nullable = false)
    private boolean travelAvailable = false;

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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getSpecialties() {
        return specialties;
    }

    public void setSpecialties(String specialties) {
        this.specialties = specialties;
    }

    public List<PhotographerSpecialty> getSpecialtiesList() {
        return PhotographerSpecialty.parseSpecialties(specialties);
    }

    public void setSpecialtiesList(List<PhotographerSpecialty> list) {
        this.specialties = PhotographerSpecialty.toCommaSeparated(list);
    }

    public List<String> getSpecialtyDisplayNames() {
        return PhotographerSpecialty.toDisplayNames(specialties);
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getInstagramUrl() {
        return instagramUrl;
    }

    public void setInstagramUrl(String instagramUrl) {
        this.instagramUrl = instagramUrl;
    }

    public String getFacebookUrl() {
        return facebookUrl;
    }

    public void setFacebookUrl(String facebookUrl) {
        this.facebookUrl = facebookUrl;
    }

    public String getEquipmentSummary() {
        return equipmentSummary;
    }

    public void setEquipmentSummary(String equipmentSummary) {
        this.equipmentSummary = equipmentSummary;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public boolean isTravelAvailable() {
        return travelAvailable;
    }

    public boolean getTravelAvailable() {
        return travelAvailable;
    }

    public void setTravelAvailable(boolean travelAvailable) {
        this.travelAvailable = travelAvailable;
    }

    public BigDecimal getStartingPrice() {
        return priceFrom;
    }

    public void setStartingPrice(BigDecimal startingPrice) {
        this.priceFrom = startingPrice;
    }
}
