package com.photoconnect.dto;

import java.math.BigDecimal;

/**
 * Request DTO for filtering and searching public approved photographers.
 *
 * All parameters are optional.
 * Blank or whitespace-only strings are normalized to null for clean database queries.
 */
public class PhotographerSearchRequest {

    private String keyword;
    private String city;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer minExperience;

    public PhotographerSearchRequest() {
    }

    public PhotographerSearchRequest(String keyword, String city, BigDecimal minPrice, BigDecimal maxPrice, Integer minExperience) {
        this.keyword = keyword;
        this.city = city;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.minExperience = minExperience;
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Integer getMinExperience() {
        return minExperience;
    }

    public void setMinExperience(Integer minExperience) {
        this.minExperience = minExperience;
    }

    // ── Normalization Helpers ────────────────────────────────────────────────

    public String getNormalizedKeyword() {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        return keyword.trim();
    }

    public String getNormalizedCity() {
        if (city == null || city.trim().isEmpty()) {
            return null;
        }
        return city.trim();
    }

    // ── Validation Helpers ───────────────────────────────────────────────────

    public boolean isValid() {
        return getValidationError() == null;
    }

    public String getValidationError() {
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            return "Minimum price cannot be negative.";
        }
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            return "Maximum price cannot be negative.";
        }
        if (minExperience != null && minExperience < 0) {
            return "Experience cannot be negative.";
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            return "Minimum price cannot exceed maximum price.";
        }
        return null;
    }

    public boolean hasFilters() {
        return getNormalizedKeyword() != null
                || getNormalizedCity() != null
                || minPrice != null
                || maxPrice != null
                || minExperience != null;
    }
}
