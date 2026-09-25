package com.photoconnect.dto;

import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerSpecialty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * View-safe DTO for public photographer marketplace pages.
 *
 * WHY A DTO instead of passing the entity directly:
 *   - The PhotographerProfile entity references User, which contains the
 *     password hash and email. Passing the entity to JSP would risk
 *     accidentally rendering sensitive fields.
 *   - A DTO defines exactly what the public is allowed to see.
 *   - Mapping happens inside the @Transactional service method, so the
 *     Hibernate session is still open when we access the LAZY User fields.
 *
 * Fields deliberately omitted:
 *   - user.password  — never public
 *   - user.email     — no product reason to expose on public marketplace
 *   - user.status    — internal account concept
 *   - verificationStatus — internal admin concept; public pages only show APPROVED
 */
public class PhotographerPublicDto {

    private final Long id;
    private final String displayName;
    private final String headline;
    private final String bio;
    private final String city;
    private final String country;
    private final Integer experienceYears;
    private final BigDecimal priceFrom;
    private final String specialties;
    private final List<String> specialtyDisplayNames;
    private final String websiteUrl;
    private final String instagramUrl;
    private final String facebookUrl;
    private final String equipmentSummary;
    private final String languages;
    private final boolean travelAvailable;
    private final LocalDateTime createdAt;
    private final String coverImageUrl;
    private final Double averageRating;
    private final Integer reviewCount;
    private final Integer profileCompleteness;

    private PhotographerPublicDto(Long id, String displayName, String headline, String bio,
                                  String city, String country, Integer experienceYears,
                                  BigDecimal priceFrom, String specialties,
                                  List<String> specialtyDisplayNames,
                                  String websiteUrl, String instagramUrl, String facebookUrl,
                                  String equipmentSummary, String languages,
                                  boolean travelAvailable,
                                  LocalDateTime createdAt,
                                  String coverImageUrl, Double averageRating,
                                  Integer reviewCount, Integer profileCompleteness) {
        this.id = id;
        this.displayName = displayName;
        this.headline = headline;
        this.bio = bio;
        this.city = city;
        this.country = country;
        this.experienceYears = experienceYears;
        this.priceFrom = priceFrom;
        this.specialties = specialties;
        this.specialtyDisplayNames = specialtyDisplayNames != null ? specialtyDisplayNames : Collections.emptyList();
        this.websiteUrl = websiteUrl;
        this.instagramUrl = instagramUrl;
        this.facebookUrl = facebookUrl;
        this.equipmentSummary = equipmentSummary;
        this.languages = languages;
        this.travelAvailable = travelAvailable;
        this.createdAt = createdAt;
        this.coverImageUrl = coverImageUrl;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
        this.profileCompleteness = profileCompleteness;
    }

    /**
     * Maps a fully-initialized PhotographerProfile into a public-safe DTO without a cover image.
     */
    public static PhotographerPublicDto from(PhotographerProfile profile) {
        return from(profile, null, null);
    }

    /**
     * Maps a fully-initialized PhotographerProfile into a public-safe DTO with an optional cover image.
     */
    public static PhotographerPublicDto from(PhotographerProfile profile, String coverImageUrl) {
        return from(profile, coverImageUrl, null);
    }

    /**
     * Maps a fully-initialized PhotographerProfile into a public-safe DTO with cover image and completeness.
     */
    public static PhotographerPublicDto from(PhotographerProfile profile, String coverImageUrl, Integer profileCompleteness) {
        List<String> displayNames = profile.getSpecialties() != null
                ? PhotographerSpecialty.toDisplayNames(profile.getSpecialties())
                : Collections.emptyList();

        return new PhotographerPublicDto(
                profile.getId(),
                profile.getDisplayName(),
                profile.getHeadline(),
                profile.getBio(),
                profile.getCity(),
                profile.getCountry(),
                profile.getExperienceYears(),
                profile.getPriceFrom(),
                profile.getSpecialties(),
                displayNames,
                profile.getWebsiteUrl(),
                profile.getInstagramUrl(),
                profile.getFacebookUrl(),
                profile.getEquipmentSummary(),
                profile.getLanguages(),
                profile.isTravelAvailable(),
                profile.getCreatedAt(),
                coverImageUrl,
                profile.getAverageRating() != null ? profile.getAverageRating() : 0.0,
                profile.getReviewCount() != null ? profile.getReviewCount() : 0,
                profileCompleteness
        );
    }

    // ── Getters ────────────────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getHeadline() {
        return headline;
    }

    public String getBio() {
        return bio;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public BigDecimal getPriceFrom() {
        return priceFrom;
    }

    public BigDecimal getStartingPrice() {
        return priceFrom;
    }

    public String getSpecialties() {
        return specialties;
    }

    public List<String> getSpecialtyDisplayNames() {
        return specialtyDisplayNames;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public String getInstagramUrl() {
        return instagramUrl;
    }

    public String getFacebookUrl() {
        return facebookUrl;
    }

    public String getEquipmentSummary() {
        return equipmentSummary;
    }

    public String getLanguages() {
        return languages;
    }

    public boolean isTravelAvailable() {
        return travelAvailable;
    }

    public boolean getTravelAvailable() {
        return travelAvailable;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public Integer getProfileCompleteness() {
        return profileCompleteness;
    }
}
