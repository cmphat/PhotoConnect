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
    private String category;
    private Double minRating;
    @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
    private java.time.LocalDate availableDate;
    private String sortBy;

    public PhotographerSearchRequest() {
    }

    public PhotographerSearchRequest(String keyword, String city, BigDecimal minPrice, BigDecimal maxPrice, Integer minExperience) {
        this.keyword = keyword;
        this.city = city;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.minExperience = minExperience;
    }

    public PhotographerSearchRequest(String keyword, String city, BigDecimal minPrice, BigDecimal maxPrice, Integer minExperience,
                                     String category, Double minRating, java.time.LocalDate availableDate, String sortBy) {
        this.keyword = keyword;
        this.city = city;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.minExperience = minExperience;
        this.category = category;
        this.minRating = minRating;
        this.availableDate = availableDate;
        this.sortBy = sortBy;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getMinRating() {
        return minRating;
    }

    public void setMinRating(Double minRating) {
        this.minRating = minRating;
    }

    public java.time.LocalDate getAvailableDate() {
        return availableDate;
    }

    public void setAvailableDate(java.time.LocalDate availableDate) {
        this.availableDate = availableDate;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
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

    public String getNormalizedCategory() {
        if (category == null || category.trim().isEmpty() || "ALL".equalsIgnoreCase(category.trim())) {
            return null;
        }
        return category.trim();
    }

    public String getNormalizedSortBy() {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return null;
        }
        return sortBy.trim().toUpperCase();
    }

    public String getEffectiveSortBy() {
        String normalized = getNormalizedSortBy();
        if (normalized == null || normalized.equals("DEFAULT")) {
            return "RECOMMENDED";
        }
        return normalized;
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
        if (getNormalizedCategory() != null && getNormalizedCategory().length() > 50) {
            return "Category cannot exceed 50 characters.";
        }
        if (minRating != null && (minRating < 0.0 || minRating > 5.0)) {
            return "Minimum rating must be between 0 and 5.";
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
        if (getNormalizedSortBy() != null && !isValidSortOption(getNormalizedSortBy())) {
            return "Invalid sort option: " + sortBy;
        }
        return null;
    }

    private boolean isValidSortOption(String sort) {
        return "RECOMMENDED".equals(sort)
                || "RATING_DESC".equals(sort)
                || "PRICE_ASC".equals(sort)
                || "PRICE_DESC".equals(sort)
                || "REVIEWS_DESC".equals(sort)
                || "COMPLETED_DESC".equals(sort)
                || "NEWEST".equals(sort)
                || "DEFAULT".equals(sort);
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
                || minExperience != null
                || getNormalizedCategory() != null
                || minRating != null
                || availableDate != null;
    }

    public boolean hasAdvancedFiltersOrSort() {
        return getNormalizedCategory() != null
                || minRating != null
                || availableDate != null
                || (getNormalizedSortBy() != null && !getNormalizedSortBy().equals("DEFAULT"));
    }
}
