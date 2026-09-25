package com.photoconnect.entity;

/**
 * Controlled photography portfolio category classification.
 *
 * Used to organize photographer portfolio images and provide
 * structured filtering on the portfolio manager and marketplace views.
 */
public enum PortfolioCategory {
    PORTRAIT("Portrait"),
    WEDDING("Wedding"),
    FASHION("Fashion"),
    LIFESTYLE("Lifestyle"),
    STREET("Street"),
    COMMERCIAL("Commercial"),
    EVENT("Event"),
    TRAVEL("Travel"),
    ARCHITECTURE("Architecture"),
    OTHER("Other");

    private final String displayName;

    PortfolioCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Converts a form input string or query parameter to the corresponding PortfolioCategory.
     * Throws IllegalArgumentException if the value is null, empty, or not a recognized category.
     */
    public static PortfolioCategory fromFormValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Portfolio category is required.");
        }
        String normalized = value.trim().toUpperCase();
        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported photography category: " + value);
        }
    }
}
