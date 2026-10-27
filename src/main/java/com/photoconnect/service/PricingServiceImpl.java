package com.photoconnect.service;

import com.photoconnect.dto.PriceBreakdownDto;
import com.photoconnect.entity.ServicePackage;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;

@Service
public class PricingServiceImpl implements PricingService {

    private static final LocalTime NIGHT_START = LocalTime.of(18, 0);
    private static final LocalTime NIGHT_END = LocalTime.of(6, 0);

    @Override
    public PriceBreakdownDto calculatePricing(BigDecimal basePrice,
                                             LocalTime shootTime,
                                             Boolean nightShoot,
                                             Integer travelDistanceKm,
                                             Integer extraHours,
                                             Integer extraPhotos,
                                             Boolean additionalPhotographer) {
        return calculatePricing(basePrice, NIGHT_SURCHARGE_RATE, FREE_TRAVEL_KM_THRESHOLD,
                TRAVEL_RATE_PER_KM, EXTRA_HOUR_RATE, EXTRA_PHOTO_RATE,
                ADDITIONAL_PHOTOGRAPHER_FEE, shootTime, nightShoot, travelDistanceKm,
                extraHours, extraPhotos, additionalPhotographer);
    }

    @Override
    public PriceBreakdownDto calculatePricing(ServicePackage servicePackage,
                                              LocalTime shootTime,
                                              Boolean nightShoot,
                                              Integer travelDistanceKm,
                                              Integer extraHours,
                                              Integer extraPhotos,
                                              Boolean additionalPhotographer) {
        if (servicePackage == null || !servicePackage.isActive()) {
            throw new IllegalArgumentException("An active service package is required for pricing.");
        }
        return calculatePricing(servicePackage.getPrice(), servicePackage.getNightSurchargeRate(),
                servicePackage.getIncludedTravelKm(), servicePackage.getExcessTravelRate(),
                servicePackage.getAdditionalHourFee(), servicePackage.getExtraEditedPhotoFee(),
                servicePackage.getAssistantFee(), shootTime, nightShoot, travelDistanceKm,
                extraHours, extraPhotos, additionalPhotographer);
    }

    private PriceBreakdownDto calculatePricing(BigDecimal basePrice,
                                              BigDecimal nightRate,
                                              Integer includedTravelKm,
                                              BigDecimal travelRate,
                                              BigDecimal hourRate,
                                              BigDecimal photoRate,
                                              BigDecimal assistantFee,
                                              LocalTime shootTime,
                                              Boolean nightShoot,
                                              Integer travelDistanceKm,
                                              Integer extraHours,
                                              Integer extraPhotos,
                                              Boolean additionalPhotographer) {
        BigDecimal base = (basePrice != null && basePrice.compareTo(BigDecimal.ZERO) > 0)
                ? basePrice.setScale(0, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 1. Night surcharge: triggered if explicitly requested OR shoot time falls between 18:00 - 06:00
        boolean isNight = Boolean.TRUE.equals(nightShoot) || isNightTime(shootTime);
        BigDecimal nightFee = isNight
                ? base.multiply(nightRate).setScale(0, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 2. Travel surcharge: 0 for <= 30 km; 15,000 VND / km for each km above 30
        int distance = (travelDistanceKm != null && travelDistanceKm > 0) ? travelDistanceKm : 0;
        BigDecimal travelFee = BigDecimal.ZERO;
        if (distance > includedTravelKm) {
            int extraKm = distance - includedTravelKm;
            travelFee = BigDecimal.valueOf(extraKm)
                    .multiply(travelRate)
                    .setScale(0, RoundingMode.HALF_UP);
        }

        // 3. Selected extras
        int hours = (extraHours != null && extraHours > 0) ? extraHours : 0;
        BigDecimal hoursFee = BigDecimal.valueOf(hours)
                .multiply(hourRate)
                .setScale(0, RoundingMode.HALF_UP);

        int photos = (extraPhotos != null && extraPhotos > 0) ? extraPhotos : 0;
        BigDecimal photosFee = BigDecimal.valueOf(photos)
                .multiply(photoRate)
                .setScale(0, RoundingMode.HALF_UP);

        boolean extraPhotographer = Boolean.TRUE.equals(additionalPhotographer);
        BigDecimal photographerFee = extraPhotographer ? assistantFee : BigDecimal.ZERO;

        BigDecimal extrasTotal = hoursFee.add(photosFee).add(photographerFee);

        // 4. Total and 30% deposit calculation
        BigDecimal total = base.add(nightFee).add(travelFee).add(extrasTotal);
        BigDecimal deposit = calculateDeposit(total);
        BigDecimal remaining = total.subtract(deposit);

        PriceBreakdownDto dto = new PriceBreakdownDto();
        dto.setBasePrice(base);
        dto.setNightSurcharge(nightFee);
        dto.setNightShoot(isNight);
        dto.setTravelDistanceKm(distance);
        dto.setTravelSurcharge(travelFee);
        dto.setExtraHours(hours);
        dto.setExtraHoursFee(hoursFee);
        dto.setExtraPhotos(photos);
        dto.setExtraPhotosFee(photosFee);
        dto.setAdditionalPhotographer(extraPhotographer);
        dto.setAdditionalPhotographerFee(photographerFee);
        dto.setExtrasFee(extrasTotal);
        dto.setTotalPrice(total);
        dto.setDepositAmount(deposit);
        dto.setRemainingBalance(remaining);

        return dto;
    }

    @Override
    public BigDecimal calculateDeposit(BigDecimal totalPrice) {
        if (totalPrice == null || totalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return totalPrice.multiply(DEPOSIT_RATE).setScale(0, RoundingMode.HALF_UP);
    }

    private boolean isNightTime(LocalTime time) {
        if (time == null) {
            return false;
        }
        return !time.isBefore(NIGHT_START) || time.isBefore(NIGHT_END);
    }
}
