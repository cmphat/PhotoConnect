package com.photoconnect.dto;

import com.photoconnect.entity.PhotographerSpecialty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Form backing request DTO for photographer professional profile editing.
 */
public class PhotographerProfileEditRequest {

    @NotBlank(message = "Professional name is required.")
    @Size(min = 2, max = 150, message = "Professional name must be between 2 and 150 characters.")
    private String displayName;

    @Size(max = 255, message = "Headline must not exceed 255 characters.")
    private String headline;

    @NotBlank(message = "Biography is required.")
    @Size(min = 20, max = 2000, message = "Biography must be between 20 and 2000 characters.")
    private String bio;

    @NotBlank(message = "City is required.")
    @Size(max = 100, message = "City must not exceed 100 characters.")
    private String city;

    @Size(max = 100, message = "Country must not exceed 100 characters.")
    private String country;

    @NotNull(message = "Years of experience is required.")
    @Min(value = 0, message = "Experience years must be 0 or more.")
    @Max(value = 80, message = "Experience years must be 80 or less.")
    private Integer experienceYears;

    @NotNull(message = "Starting price is required.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Starting price must be zero or positive.")
    @Digits(integer = 16, fraction = 2, message = "Starting price must have at most 16 integer digits and 2 decimal places.")
    private BigDecimal priceFrom;

    private List<String> specialties = new ArrayList<>();

    @Size(max = 255, message = "Website URL must not exceed 255 characters.")
    private String websiteUrl;

    @Size(max = 255, message = "Instagram URL must not exceed 255 characters.")
    private String instagramUrl;

    @Size(max = 255, message = "Facebook URL must not exceed 255 characters.")
    private String facebookUrl;

    @Size(max = 500, message = "Equipment summary must not exceed 500 characters.")
    private String equipmentSummary;

    @Size(max = 150, message = "Languages must not exceed 150 characters.")
    private String languages;

    private Boolean travelAvailable = false;

    public PhotographerProfileEditRequest() {
    }

    public static boolean isValidHttpUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return true;
        }
        String trimmed = url.trim();
        if (trimmed.length() > 255) {
            return false;
        }
        String lower = trimmed.toLowerCase();
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            return false;
        }
        if (lower.contains("javascript:") || lower.contains("data:") || lower.contains("file:") || trimmed.contains(" ")) {
            return false;
        }
        try {
            URI uri = URI.create(trimmed);
            String scheme = uri.getScheme();
            return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
        } catch (Exception e) {
            return false;
        }
    }

    public String validateBusinessRules() {
        if (!isValidHttpUrl(websiteUrl)) {
            return "Website URL must start with http:// or https:// and cannot contain scripts or invalid characters.";
        }
        if (!isValidHttpUrl(instagramUrl)) {
            return "Instagram URL must start with http:// or https:// and cannot contain scripts or invalid characters.";
        }
        if (!isValidHttpUrl(facebookUrl)) {
            return "Facebook URL must start with http:// or https:// and cannot contain scripts or invalid characters.";
        }
        if (specialties != null) {
            for (String s : specialties) {
                if (s != null && !s.trim().isEmpty() && !PhotographerSpecialty.isValid(s)) {
                    return "Invalid photography specialty selected: " + s;
                }
            }
        }
        return null;
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
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

    public List<String> getSpecialties() {
        return specialties;
    }

    public void setSpecialties(List<String> specialties) {
        this.specialties = specialties;
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

    public Boolean getTravelAvailable() {
        return travelAvailable != null ? travelAvailable : false;
    }

    public void setTravelAvailable(Boolean travelAvailable) {
        this.travelAvailable = travelAvailable;
    }
}
