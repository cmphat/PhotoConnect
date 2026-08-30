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
}
