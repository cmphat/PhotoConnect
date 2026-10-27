package com.photoconnect.controller;

import com.photoconnect.dto.PriceBreakdownDto;
import com.photoconnect.entity.ServicePackage;
import com.photoconnect.repository.ServicePackageRepository;
import com.photoconnect.service.PricingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;

@RestController
@RequestMapping("/api/pricing")
public class PricingApiController {

    private final PricingService pricingService;
    private final ServicePackageRepository servicePackageRepository;
    public PricingApiController(PricingService pricingService,
                                ServicePackageRepository servicePackageRepository) {
        this.pricingService = pricingService;
        this.servicePackageRepository = servicePackageRepository;
    }

    @GetMapping("/calculate")
    public ResponseEntity<PriceBreakdownDto> calculate(
            @RequestParam(value = "photographerId", required = false) Long photographerId,
            @RequestParam("packageId") Long packageId,
            @RequestParam(value = "bookingTime", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime bookingTime,
            @RequestParam(value = "nightShoot", required = false, defaultValue = "false") Boolean nightShoot,
            @RequestParam(value = "travelDistanceKm", required = false, defaultValue = "0") Integer travelDistanceKm,
            @RequestParam(value = "extraHours", required = false, defaultValue = "0") Integer extraHours,
            @RequestParam(value = "extraPhotos", required = false, defaultValue = "0") Integer extraPhotos,
            @RequestParam(value = "additionalPhotographer", required = false, defaultValue = "false") Boolean additionalPhotographer
    ) {
        if (photographerId == null) {
            return ResponseEntity.badRequest().build();
        }
        ServicePackage pkg = servicePackageRepository
                .findByIdAndPhotographerProfileId(packageId, photographerId)
                .filter(ServicePackage::isActive)
                .orElse(null);
        if (pkg == null) {
            return ResponseEntity.notFound().build();
        }

        PriceBreakdownDto breakdown = pricingService.calculatePricing(
                pkg,
                bookingTime,
                nightShoot,
                travelDistanceKm,
                extraHours,
                extraPhotos,
                additionalPhotographer
        );

        return ResponseEntity.ok(breakdown);
    }
}
