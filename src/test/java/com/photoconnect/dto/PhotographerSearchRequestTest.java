package com.photoconnect.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PhotographerSearchRequestTest {

    @Test
    void shouldNormalizeBlankValuesToNull() {
        PhotographerSearchRequest request = new PhotographerSearchRequest();
        request.setKeyword("   ");
        request.setCity("  ");

        assertThat(request.getNormalizedKeyword()).isNull();
        assertThat(request.getNormalizedCity()).isNull();
    }

    @Test
    void shouldTrimNonBlankValues() {
        PhotographerSearchRequest request = new PhotographerSearchRequest();
        request.setKeyword("  wedding portrait  ");
        request.setCity("  Da Nang  ");

        assertThat(request.getNormalizedKeyword()).isEqualTo("wedding portrait");
        assertThat(request.getNormalizedCity()).isEqualTo("Da Nang");
    }

    @Test
    void shouldValidateNonNegativePriceAndExperience() {
        PhotographerSearchRequest request = new PhotographerSearchRequest();
        request.setMinPrice(new BigDecimal("-100"));
        assertThat(request.isValid()).isFalse();
        assertThat(request.getValidationError()).contains("Minimum price cannot be negative");

        request = new PhotographerSearchRequest();
        request.setMaxPrice(new BigDecimal("-50"));
        assertThat(request.isValid()).isFalse();
        assertThat(request.getValidationError()).contains("Maximum price cannot be negative");

        request = new PhotographerSearchRequest();
        request.setMinExperience(-1);
        assertThat(request.isValid()).isFalse();
        assertThat(request.getValidationError()).contains("Experience cannot be negative");
    }

    @Test
    void shouldValidateMinPriceLessThanOrEqualToMaxPrice() {
        PhotographerSearchRequest request = new PhotographerSearchRequest();
        request.setMinPrice(new BigDecimal("5000000"));
        request.setMaxPrice(new BigDecimal("2000000"));

        assertThat(request.isValid()).isFalse();
        assertThat(request.getValidationError()).contains("Minimum price cannot exceed maximum price");
    }

    @Test
    void shouldRejectPriceFiltersThatExceedDatabasePrecisionOrScale() {
        PhotographerSearchRequest request = new PhotographerSearchRequest();
        request.setMinPrice(new BigDecimal("123.456"));
        assertThat(request.getValidationError()).contains("Minimum price must have at most 16 integer digits and 2 decimal places");

        request = new PhotographerSearchRequest();
        request.setMaxPrice(new BigDecimal("12345678901234567.00"));
        assertThat(request.getValidationError()).contains("Maximum price must have at most 16 integer digits and 2 decimal places");

        request = new PhotographerSearchRequest();
        request.setMinPrice(new BigDecimal("9999999999999999.99"));
        assertThat(request.isValid()).isTrue();
    }

    @Test
    void shouldPassValidationWhenFiltersAreValid() {
        PhotographerSearchRequest request = new PhotographerSearchRequest();
        request.setKeyword("fashion");
        request.setCity("Ho Chi Minh City");
        request.setMinPrice(new BigDecimal("1000000"));
        request.setMaxPrice(new BigDecimal("5000000"));
        request.setMinExperience(3);

        assertThat(request.isValid()).isTrue();
        assertThat(request.getValidationError()).isNull();
        assertThat(request.hasFilters()).isTrue();
    }

    @Test
    void shouldReportNoFiltersWhenEmpty() {
        PhotographerSearchRequest request = new PhotographerSearchRequest();
        assertThat(request.hasFilters()).isFalse();
        assertThat(request.isValid()).isTrue();
    }

    @Test
    void shouldRejectOversizedTextAndExperienceFilters() {
        PhotographerSearchRequest request = new PhotographerSearchRequest();
        request.setKeyword("x".repeat(101));
        assertThat(request.getValidationError()).contains("keyword cannot exceed 100");

        request = new PhotographerSearchRequest();
        request.setCity("x".repeat(101));
        assertThat(request.getValidationError()).contains("City cannot exceed 100");

        request = new PhotographerSearchRequest();
        request.setMinExperience(81);
        assertThat(request.getValidationError()).contains("Experience cannot exceed 80");
    }

    @Test
    void shouldValidatePageBoundsWithoutTreatingPageAsFilter() {
        PhotographerSearchRequest request = new PhotographerSearchRequest();
        request.setPage(-1);
        assertThat(request.getValidationError()).contains("Page must be between");

        request.setPage(2);
        assertThat(request.isValid()).isTrue();
        assertThat(request.getPageOrDefault()).isEqualTo(2);
        assertThat(request.hasFilters()).isFalse();
    }
}
