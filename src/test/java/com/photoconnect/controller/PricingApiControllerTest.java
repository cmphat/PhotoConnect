package com.photoconnect.controller;

import com.photoconnect.dto.PriceBreakdownDto;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.ServicePackage;
import com.photoconnect.repository.ServicePackageRepository;
import com.photoconnect.service.PricingService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PricingApiControllerTest {

    @Test
    void usesOnlyActivePackageOwnedByRequestedPhotographer() {
        PricingService pricing = mock(PricingService.class);
        ServicePackageRepository packages = mock(ServicePackageRepository.class);
        PricingApiController controller = new PricingApiController(pricing, packages);
        PhotographerProfile profile = new PhotographerProfile();
        profile.setId(7L);
        ServicePackage pkg = new ServicePackage();
        pkg.setId(9L);
        pkg.setPhotographerProfile(profile);
        pkg.setPrice(new BigDecimal("1200000"));
        pkg.setActive(true);
        PriceBreakdownDto expected = new PriceBreakdownDto();
        expected.setBasePrice(pkg.getPrice());
        when(packages.findByIdAndPhotographerProfileId(9L, 7L)).thenReturn(Optional.of(pkg));
        when(pricing.calculatePricing(pkg, LocalTime.NOON, false, 0, 0, 0, false)).thenReturn(expected);

        var response = controller.calculate(7L, 9L, LocalTime.NOON, false, 0, 0, 0, false);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().getBasePrice()).isEqualByComparingTo("1200000");
        verify(packages).findByIdAndPhotographerProfileId(9L, 7L);
    }

    @Test
    void rejectsPackageOwnedByAnotherPhotographerOrInactive() {
        PricingService pricing = mock(PricingService.class);
        ServicePackageRepository packages = mock(ServicePackageRepository.class);
        PricingApiController controller = new PricingApiController(pricing, packages);
        when(packages.findByIdAndPhotographerProfileId(9L, 7L)).thenReturn(Optional.empty());

        var response = controller.calculate(7L, 9L, LocalTime.NOON, false, 0, 0, 0, false);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        verifyNoInteractions(pricing);
    }
}
