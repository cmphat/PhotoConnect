package com.photoconnect.dto;

import com.photoconnect.entity.PhotographerProfile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * View-safe DTO for public photographer marketplace pages.
 *
 * WHY A DTO instead of passing the entity directly:
 *   - The PhotographerProfile entity references User, which contains the
 *     password hash and email. Passing the entity to JSP would risk
 *     accidentally rendering sensitive fields.
 *   - A DTO defines exactly what the public is allowed to see.
 *   - Mapping happens inside the @Transactional service method, so the
 *     Hibernate session is still open when we access the LAZY User fields
 *     (even though we already JOIN FETCH them — belt-and-suspenders).
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
    private final String bio;
    private final String city;
    private final Integer experienceYears;
    private final BigDecimal priceFrom;
    private final LocalDateTime createdAt;
    private final String coverImageUrl;

    private PhotographerPublicDto(Long id, String displayName, String bio,
                                   String city, Integer experienceYears,
                                   BigDecimal priceFrom, LocalDateTime createdAt,
                                   String coverImageUrl) {
        this.id = id;
        this.displayName = displayName;
        this.bio = bio;
        this.city = city;
        this.experienceYears = experienceYears;
        this.priceFrom = priceFrom;
        this.createdAt = createdAt;
        this.coverImageUrl = coverImageUrl;
    }

    /**
     * Maps a fully-initialized PhotographerProfile (with its User JOIN FETCHed)
     * into a public-safe DTO without a cover image.
     */
    public static PhotographerPublicDto from(PhotographerProfile profile) {
        return from(profile, null);
    }

    /**
     * Maps a fully-initialized PhotographerProfile into a public-safe DTO with an optional cover image.
     */
    public static PhotographerPublicDto from(PhotographerProfile profile, String coverImageUrl) {
        return new PhotographerPublicDto(
                profile.getId(),
                profile.getDisplayName(),
                profile.getBio(),
                profile.getCity(),
                profile.getExperienceYears(),
                profile.getPriceFrom(),
                profile.getCreatedAt(),
                coverImageUrl
        );
    }

    // ── Getters ────────────────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBio() {
        return bio;
    }

    public String getCity() {
        return city;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public BigDecimal getPriceFrom() {
        return priceFrom;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }
}
