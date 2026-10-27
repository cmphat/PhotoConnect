package com.photoconnect.service;

import com.photoconnect.dto.PriceBreakdownDto;
import com.photoconnect.entity.ServicePackage;

import java.math.BigDecimal;
import java.time.LocalTime;

public interface PricingService {

    BigDecimal NIGHT_SURCHARGE_RATE = new BigDecimal("0.20"); // 20%
    int FREE_TRAVEL_KM_THRESHOLD = 30; // Free up to 30 km
    BigDecimal TRAVEL_RATE_PER_KM = new BigDecimal("15000"); // 15,000 VND/km beyond 30 km
    BigDecimal EXTRA_HOUR_RATE = new BigDecimal("200000"); // 200,000 VND/hour
    BigDecimal EXTRA_PHOTO_RATE = new BigDecimal("50000"); // 50,000 VND/photo
    BigDecimal ADDITIONAL_PHOTOGRAPHER_FEE = new BigDecimal("500000"); // 500,000 VND
    BigDecimal DEPOSIT_RATE = new BigDecimal("0.30"); // 30% deposit

    PriceBreakdownDto calculatePricing(
            BigDecimal basePrice,
            LocalTime shootTime,
            Boolean nightShoot,
            Integer travelDistanceKm,
            Integer extraHours,
            Integer extraPhotos,
            Boolean additionalPhotographer
    );

    PriceBreakdownDto calculatePricing(
            ServicePackage servicePackage,
            LocalTime shootTime,
            Boolean nightShoot,
            Integer travelDistanceKm,
            Integer extraHours,
            Integer extraPhotos,
            Boolean additionalPhotographer
    );

    BigDecimal calculateDeposit(BigDecimal totalPrice);
}
