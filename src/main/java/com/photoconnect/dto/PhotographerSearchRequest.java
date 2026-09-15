package com.photoconnect.dto;

import java.math.BigDecimal;

/**
 * Request DTO for filtering and searching public approved photographers.
 *
 * All parameters are optional.
 * Blank or whitespace-only strings are normalized to null for clean database queries.
 */
public class PhotographerSearchRequest {

    public static final int MAX_TEXT_LENGTH = 100;
    public static final int MAX_EXPERIENCE_YEARS = 80;
    public static final int MAX_PRICE_INTEGER_DIGITS = 16;
    public static final int MAX_PRICE_FRACTION_DIGITS = 2;

    private String keyword;
    private String city;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer minExperience;
    private Integer page;

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

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public int getPageOrDefault() {
        return page != null ? page : 0;
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
        if (page != null && (page < 0 || page > 10_000)) {
            return "Page must be between 0 and 10000.";
        }
        if (getNormalizedKeyword() != null && getNormalizedKeyword().length() > MAX_TEXT_LENGTH) {
            return "Search keyword cannot exceed 100 characters.";
        }
        if (getNormalizedCity() != null && getNormalizedCity().length() > MAX_TEXT_LENGTH) {
            return "City cannot exceed 100 characters.";
        }
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            return "Minimum price cannot be negative.";
        }
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            return "Maximum price cannot be negative.";
        }
        if (!hasValidPriceShape(minPrice)) {
            return "Minimum price must have at most 16 integer digits and 2 decimal places.";
        }
        if (!hasValidPriceShape(maxPrice)) {
            return "Maximum price must have at most 16 integer digits and 2 decimal places.";
        }
        if (minExperience != null && minExperience < 0) {
            return "Experience cannot be negative.";
        }
        if (minExperience != null && minExperience > MAX_EXPERIENCE_YEARS) {
            return "Experience cannot exceed 80 years.";
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            return "Minimum price cannot exceed maximum price.";
        }
        return null;
    }

    private boolean hasValidPriceShape(BigDecimal price) {
        if (price == null) {
            return true;
        }
        int fractionDigits = Math.max(price.scale(), 0);
        int integerDigits = Math.max(price.precision() - price.scale(), 0);
        return integerDigits <= MAX_PRICE_INTEGER_DIGITS
                && fractionDigits <= MAX_PRICE_FRACTION_DIGITS;
    }

    public boolean hasFilters() {
        return getNormalizedKeyword() != null
                || getNormalizedCity() != null
                || minPrice != null
                || maxPrice != null
                || minExperience != null;
    }
}
