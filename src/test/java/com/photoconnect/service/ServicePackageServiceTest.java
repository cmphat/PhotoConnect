package com.photoconnect.service;

import com.photoconnect.dto.ServicePackageDto;
import com.photoconnect.dto.ServicePackageRequest;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PortfolioCategory;
import com.photoconnect.entity.ServicePackage;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.exception.ResourceNotFoundException;
import com.photoconnect.exception.UnauthorizedException;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.repository.ServicePackageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicePackageServiceTest {

    @Mock
    private ServicePackageRepository servicePackageRepository;

    @Mock
    private PhotographerProfileRepository photographerProfileRepository;

    @InjectMocks
    private ServicePackageServiceImpl servicePackageService;

    private User photographerUser;
    private PhotographerProfile profile;
    private ServicePackage pkg;
    private ServicePackageRequest validRequest;

    @BeforeEach
    void setUp() {
        photographerUser = new User();
        photographerUser.setId(2L);
        photographerUser.setRole(UserRole.PHOTOGRAPHER);
        photographerUser.setStatus(UserStatus.ACTIVE);

        profile = new PhotographerProfile();
        profile.setId(10L);
        profile.setUser(photographerUser);

        pkg = new ServicePackage();
        pkg.setId(100L);
        pkg.setPhotographerProfile(profile);
        pkg.setCategory(PortfolioCategory.WEDDING);
        pkg.setName("Standard Wedding Package");
        pkg.setDescription("Complete ceremony and reception coverage");
        pkg.setPrice(BigDecimal.valueOf(5000000));
        pkg.setDurationMinutes(240);
        pkg.setEditedPhotoCount(100);
        pkg.setDeliveryDays(7);
        pkg.setActive(true);

        validRequest = new ServicePackageRequest();
        validRequest.setCategory("WEDDING");
        validRequest.setName("Standard Wedding Package");
        validRequest.setDescription("Complete ceremony and reception coverage");
        validRequest.setPrice(BigDecimal.valueOf(5000000));
        validRequest.setDurationMinutes(240);
        validRequest.setEditedPhotoCount(100);
        validRequest.setDeliveryDays(7);
    }

    @Test
    void createPackage_Success() {
        when(photographerProfileRepository.findByUserIdWithUser(2L)).thenReturn(Optional.of(profile));
        when(servicePackageRepository.save(any(ServicePackage.class))).thenAnswer(invocation -> {
            ServicePackage saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        ServicePackageDto result = servicePackageService.createPackage(2L, validRequest);

        assertNotNull(result);
        assertEquals("Standard Wedding Package", result.getName());
        assertEquals(BigDecimal.valueOf(5000000), result.getPrice());
        assertEquals(240, result.getDurationMinutes());
        assertEquals(100, result.getEditedPhotoCount());
        assertEquals(7, result.getDeliveryDays());
        assertTrue(result.isActive());
        verify(servicePackageRepository, times(1)).save(any(ServicePackage.class));
    }

    @Test
    void createPackage_ProfileNotFound_ThrowsUnauthorizedException() {
        when(photographerProfileRepository.findByUserIdWithUser(99L)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () ->
                servicePackageService.createPackage(99L, validRequest));
    }

    @Test
    void createPackage_InvalidPrice_ThrowsException() {
        validRequest.setPrice(BigDecimal.ZERO);
        assertThrows(IllegalArgumentException.class, () ->
                servicePackageService.createPackage(2L, validRequest));

        validRequest.setPrice(BigDecimal.valueOf(-100));
        assertThrows(IllegalArgumentException.class, () ->
                servicePackageService.createPackage(2L, validRequest));

        validRequest.setPrice(null);
        assertThrows(IllegalArgumentException.class, () ->
                servicePackageService.createPackage(2L, validRequest));
    }

    @Test
    void createPackage_InvalidDuration_ThrowsException() {
        validRequest.setDurationMinutes(0);
        assertThrows(IllegalArgumentException.class, () ->
                servicePackageService.createPackage(2L, validRequest));

        validRequest.setDurationMinutes(-30);
        assertThrows(IllegalArgumentException.class, () ->
                servicePackageService.createPackage(2L, validRequest));
    }

    @Test
    void createPackage_InvalidPhotoCount_ThrowsException() {
        validRequest.setEditedPhotoCount(-5);
        assertThrows(IllegalArgumentException.class, () ->
                servicePackageService.createPackage(2L, validRequest));
    }

    @Test
    void createPackage_InvalidDeliveryDays_ThrowsException() {
        validRequest.setDeliveryDays(0);
        assertThrows(IllegalArgumentException.class, () ->
                servicePackageService.createPackage(2L, validRequest));
    }

    @Test
    void updatePackage_Success() {
        when(photographerProfileRepository.findByUserIdWithUser(2L)).thenReturn(Optional.of(profile));
        when(servicePackageRepository.findByIdAndPhotographerProfileId(100L, 10L)).thenReturn(Optional.of(pkg));
        when(servicePackageRepository.save(any(ServicePackage.class))).thenReturn(pkg);

        validRequest.setName("Updated Deluxe Package");
        validRequest.setPrice(BigDecimal.valueOf(7000000));

        ServicePackageDto updated = servicePackageService.updatePackage(2L, 100L, validRequest);

        assertNotNull(updated);
        assertEquals("Updated Deluxe Package", updated.getName());
        assertEquals(BigDecimal.valueOf(7000000), updated.getPrice());
        verify(servicePackageRepository, times(1)).save(pkg);
    }

    @Test
    void updatePackage_NotFoundOrOwnershipMismatch_ThrowsIllegalArgumentException() {
        when(photographerProfileRepository.findByUserIdWithUser(2L)).thenReturn(Optional.of(profile));
        when(servicePackageRepository.findByIdAndPhotographerProfileId(100L, 10L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                servicePackageService.updatePackage(2L, 100L, validRequest));
    }

    @Test
    void togglePackageActive_Success() {
        when(photographerProfileRepository.findByUserIdWithUser(2L)).thenReturn(Optional.of(profile));
        when(servicePackageRepository.findByIdAndPhotographerProfileId(100L, 10L)).thenReturn(Optional.of(pkg));
        when(servicePackageRepository.save(any(ServicePackage.class))).thenReturn(pkg);

        ServicePackageDto result = servicePackageService.togglePackageActive(2L, 100L, false);

        assertFalse(result.isActive());
        verify(servicePackageRepository, times(1)).save(pkg);
    }

    @Test
    void deletePackage_Success() {
        when(photographerProfileRepository.findByUserIdWithUser(2L)).thenReturn(Optional.of(profile));
        when(servicePackageRepository.findByIdAndPhotographerProfileId(100L, 10L)).thenReturn(Optional.of(pkg));

        servicePackageService.deletePackage(2L, 100L);

        verify(servicePackageRepository, times(1)).delete(pkg);
    }

    @Test
    void deletePackage_NotFoundOrOwnershipMismatch_ThrowsIllegalArgumentException() {
        when(photographerProfileRepository.findByUserIdWithUser(2L)).thenReturn(Optional.of(profile));
        when(servicePackageRepository.findByIdAndPhotographerProfileId(100L, 10L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                servicePackageService.deletePackage(2L, 100L));
    }

    @Test
    void getActivePackagesForPhotographer_ReturnsList() {
        when(servicePackageRepository.findByPhotographerProfileIdAndActiveTrueOrderByPriceAsc(10L))
                .thenReturn(Collections.singletonList(pkg));

        List<ServicePackageDto> list = servicePackageService.getActivePackagesForPhotographer(10L);

        assertEquals(1, list.size());
        assertEquals("Standard Wedding Package", list.get(0).getName());
    }
}
