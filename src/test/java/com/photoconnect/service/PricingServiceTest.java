package com.photoconnect.service;

import com.photoconnect.dto.PriceBreakdownDto;
import com.photoconnect.entity.ServicePackage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class PricingServiceTest {

    private final PricingService pricingService = new PricingServiceImpl();

    @Test
    @DisplayName("Base price is properly captured and rounded to whole VND")
    void testBasePriceCalculation() {
        PriceBreakdownDto breakdown = pricingService.calculatePricing(
                new BigDecimal("1500000"),
                LocalTime.of(10, 0),
                false,
                10,
                0,
                0,
                false
        );

        assertThat(breakdown.getBasePrice()).isEqualByComparingTo("1500000");
        assertThat(breakdown.getNightSurcharge()).isEqualByComparingTo("0");
        assertThat(breakdown.getTravelSurcharge()).isEqualByComparingTo("0");
        assertThat(breakdown.getExtrasFee()).isEqualByComparingTo("0");
        assertThat(breakdown.getTotalPrice()).isEqualByComparingTo("1500000");
        assertThat(breakdown.getDepositAmount()).isEqualByComparingTo("450000");
        assertThat(breakdown.getRemainingBalance()).isEqualByComparingTo("1050000");
    }

    @Test
    @DisplayName("Night surcharge of 20% applies for shoot time >= 18:00, < 06:00, or explicit night flag")
    void testNightSurcharge() {
        // Evening 19:00 -> 20%
        PriceBreakdownDto evening = pricingService.calculatePricing(
                new BigDecimal("1000000"),
                LocalTime.of(19, 0),
                false,
                0, 0, 0, false
        );
        assertThat(evening.isNightShoot()).isTrue();
        assertThat(evening.getNightSurcharge()).isEqualByComparingTo("200000");

        // Early morning 05:00 -> 20%
        PriceBreakdownDto dawn = pricingService.calculatePricing(
                new BigDecimal("1000000"),
                LocalTime.of(5, 0),
                false,
                0, 0, 0, false
        );
        assertThat(dawn.isNightShoot()).isTrue();
        assertThat(dawn.getNightSurcharge()).isEqualByComparingTo("200000");

        // Daytime 14:00 with explicit nightShoot flag -> 20%
        PriceBreakdownDto explicit = pricingService.calculatePricing(
                new BigDecimal("1000000"),
                LocalTime.of(14, 0),
                true,
                0, 0, 0, false
        );
        assertThat(explicit.isNightShoot()).isTrue();
        assertThat(explicit.getNightSurcharge()).isEqualByComparingTo("200000");

        // Regular daytime 14:00 without flag -> 0
        PriceBreakdownDto regular = pricingService.calculatePricing(
                new BigDecimal("1000000"),
                LocalTime.of(14, 0),
                false,
                0, 0, 0, false
        );
        assertThat(regular.isNightShoot()).isFalse();
        assertThat(regular.getNightSurcharge()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("Travel threshold: exactly 30 km is free (0 VND), beyond 30 km charges 15,000 VND / km")
    void testTravelSurcharges() {
        // At threshold (30 km) -> 0 fee
        PriceBreakdownDto atThreshold = pricingService.calculatePricing(
                new BigDecimal("1000000"),
                LocalTime.of(10, 0),
                false,
                30,
                0, 0, false
        );
        assertThat(atThreshold.getTravelSurcharge()).isEqualByComparingTo("0");

        // Under threshold (15 km) -> 0 fee
        PriceBreakdownDto underThreshold = pricingService.calculatePricing(
                new BigDecimal("1000000"),
                LocalTime.of(10, 0),
                false,
                15,
                0, 0, false
        );
        assertThat(underThreshold.getTravelSurcharge()).isEqualByComparingTo("0");

        // 35 km -> (35 - 30) * 15,000 = 75,000 VND
        PriceBreakdownDto aboveThreshold = pricingService.calculatePricing(
                new BigDecimal("1000000"),
                LocalTime.of(10, 0),
                false,
                35,
                0, 0, false
        );
        assertThat(aboveThreshold.getTravelSurcharge()).isEqualByComparingTo("75000");
    }

    @Test
    @DisplayName("Selected extras calculate correctly: 200,000/hr, 50,000/photo, 500,000 2nd photographer")
    void testExtrasCalculation() {
        PriceBreakdownDto breakdown = pricingService.calculatePricing(
                new BigDecimal("1000000"),
                LocalTime.of(10, 0),
                false,
                0,
                2,   // 2 extra hours = 400,000
                4,   // 4 extra photos = 200,000
                true // 2nd photographer = 500,000
        );

        assertThat(breakdown.getExtraHours()).isEqualTo(2);
        assertThat(breakdown.getExtraHoursFee()).isEqualByComparingTo("400000");
        assertThat(breakdown.getExtraPhotos()).isEqualTo(4);
        assertThat(breakdown.getExtraPhotosFee()).isEqualByComparingTo("200000");
        assertThat(breakdown.isAdditionalPhotographer()).isTrue();
        assertThat(breakdown.getAdditionalPhotographerFee()).isEqualByComparingTo("500000");
        assertThat(breakdown.getExtrasFee()).isEqualByComparingTo("1100000");
        assertThat(breakdown.getTotalPrice()).isEqualByComparingTo("2100000");
        assertThat(breakdown.getDepositAmount()).isEqualByComparingTo("630000");
        assertThat(breakdown.getRemainingBalance()).isEqualByComparingTo("1470000");
    }

    @Test
    @DisplayName("Section 3 specification example: 1,200,000 base + 240,000 night + 75,000 travel (35km) = 1,515,000 total")
    void testSpecificationFullExample() {
        PriceBreakdownDto breakdown = pricingService.calculatePricing(
                new BigDecimal("1200000"),
                LocalTime.of(19, 30), // night shoot
                false,
                35,                   // 5 km over 30 km threshold -> 75,000
                0,
                0,
                false
        );

        assertThat(breakdown.getBasePrice()).isEqualByComparingTo("1200000");
        assertThat(breakdown.getNightSurcharge()).isEqualByComparingTo("240000");
        assertThat(breakdown.getTravelSurcharge()).isEqualByComparingTo("75000");
        assertThat(breakdown.getExtrasFee()).isEqualByComparingTo("0");
        assertThat(breakdown.getTotalPrice()).isEqualByComparingTo("1515000");

        // 30% deposit of 1,515,000 = 454,500 VND
        assertThat(breakdown.getDepositAmount()).isEqualByComparingTo("454500");
        // 70% remaining balance = 1,060,500 VND
        assertThat(breakdown.getRemainingBalance()).isEqualByComparingTo("1060500");
        // Invariant: deposit + remaining = total
        assertThat(breakdown.getDepositAmount().add(breakdown.getRemainingBalance()))
                .isEqualByComparingTo(breakdown.getTotalPrice());
    }

    @Test
    @DisplayName("VND rounding uses HALF_UP to whole VND")
    void testVndRoundingHalfUp() {
        // 1,000,001 * 0.30 = 300,000.30 -> 300,000
        BigDecimal dep1 = pricingService.calculateDeposit(new BigDecimal("1000001"));
        assertThat(dep1).isEqualByComparingTo("300000");

        // 1,000,005 * 0.30 = 300,001.50 -> 300,002 (HALF_UP)
        BigDecimal dep2 = pricingService.calculateDeposit(new BigDecimal("1000005"));
        assertThat(dep2).isEqualByComparingTo("300002");
    }

    @Test
    @DisplayName("Package-specific rates are authoritative")
    void packageSpecificRatesAreUsed() {
        ServicePackage pkg = new ServicePackage();
        pkg.setActive(true);
        pkg.setPrice(new BigDecimal("1000000"));
        pkg.setNightSurchargeRate(new BigDecimal("0.10"));
        pkg.setIncludedTravelKm(5);
        pkg.setExcessTravelRate(new BigDecimal("20000"));
        pkg.setAdditionalHourFee(new BigDecimal("300000"));
        pkg.setExtraEditedPhotoFee(new BigDecimal("25000"));
        pkg.setAssistantFee(new BigDecimal("700000"));

        PriceBreakdownDto result = pricingService.calculatePricing(
                pkg, LocalTime.of(20, 0), false, 8, 2, 4, true);

        assertThat(result.getNightSurcharge()).isEqualByComparingTo("100000");
        assertThat(result.getTravelSurcharge()).isEqualByComparingTo("60000");
        assertThat(result.getExtrasFee()).isEqualByComparingTo("1400000");
        assertThat(result.getTotalPrice()).isEqualByComparingTo("2560000");
        assertThat(result.getDepositAmount()).isEqualByComparingTo("768000");
    }
}
