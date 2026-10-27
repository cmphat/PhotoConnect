package com.photoconnect.service;

import com.photoconnect.dto.ServicePackageDto;
import com.photoconnect.dto.ServicePackageRequest;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PortfolioCategory;
import com.photoconnect.entity.ServicePackage;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.exception.UnauthorizedException;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.repository.ServicePackageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ServicePackageServiceImpl implements ServicePackageService {

    private static final Logger log = LoggerFactory.getLogger(ServicePackageServiceImpl.class);

    private final ServicePackageRepository servicePackageRepository;
    private final PhotographerProfileRepository photographerProfileRepository;

    public ServicePackageServiceImpl(ServicePackageRepository servicePackageRepository,
                                     PhotographerProfileRepository photographerProfileRepository) {
        this.servicePackageRepository = servicePackageRepository;
        this.photographerProfileRepository = photographerProfileRepository;
    }

    @Override
    public ServicePackageDto createPackage(Long photographerUserId, ServicePackageRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Service package request cannot be null.");
        }
        request.validate();

        PhotographerProfile profile = requireActivePhotographerProfile(photographerUserId);

        long activeCount = servicePackageRepository.countByPhotographerProfileIdAndActiveTrue(profile.getId());
        if (activeCount >= 10) {
            throw new IllegalArgumentException("Photographer cannot have more than 10 active service packages.");
        }

        ServicePackage servicePackage = new ServicePackage(
                profile,
                PortfolioCategory.fromFormValue(request.getCategory()),
                request.getName().trim(),
                request.getDescription() != null ? request.getDescription().trim() : null,
                request.getPrice(),
                request.getDurationMinutes(),
                request.getEditedPhotoCount(),
                request.getDeliveryDays()
        );
        applyPricingSettings(servicePackage, request);

        ServicePackage saved = servicePackageRepository.save(servicePackage);
        log.info("Created service package [{}] for photographer profile [{}]", saved.getId(), profile.getId());
        return ServicePackageDto.fromEntity(saved);
    }

    @Override
    public ServicePackageDto updatePackage(Long photographerUserId, Long packageId, ServicePackageRequest request) {
        if (packageId == null || request == null) {
            throw new IllegalArgumentException("Package ID and request cannot be null.");
        }
        request.validate();

        PhotographerProfile profile = requireActivePhotographerProfile(photographerUserId);

        ServicePackage servicePackage = servicePackageRepository.findByIdAndPhotographerProfileId(packageId, profile.getId())
                .orElseThrow(() -> new IllegalArgumentException("Service package not found or does not belong to your profile."));

        servicePackage.setName(request.getName().trim());
        servicePackage.setCategory(PortfolioCategory.fromFormValue(request.getCategory()));
        servicePackage.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        servicePackage.setPrice(request.getPrice());
        servicePackage.setDurationMinutes(request.getDurationMinutes());
        servicePackage.setEditedPhotoCount(request.getEditedPhotoCount());
        servicePackage.setDeliveryDays(request.getDeliveryDays());
        applyPricingSettings(servicePackage, request);

        ServicePackage updated = servicePackageRepository.save(servicePackage);
        log.info("Updated service package [{}] for photographer profile [{}]", updated.getId(), profile.getId());
        return ServicePackageDto.fromEntity(updated);
    }

    @Override
    public ServicePackageDto togglePackageActive(Long photographerUserId, Long packageId, boolean active) {
        if (packageId == null) {
            throw new IllegalArgumentException("Package ID cannot be null.");
        }

        PhotographerProfile profile = requireActivePhotographerProfile(photographerUserId);

        ServicePackage servicePackage = servicePackageRepository.findByIdAndPhotographerProfileId(packageId, profile.getId())
                .orElseThrow(() -> new IllegalArgumentException("Service package not found or does not belong to your profile."));

        if (active && !servicePackage.isActive()) {
            long activeCount = servicePackageRepository.countByPhotographerProfileIdAndActiveTrue(profile.getId());
            if (activeCount >= 10) {
                throw new IllegalArgumentException("Photographer cannot have more than 10 active service packages.");
            }
        }

        servicePackage.setActive(active);
        ServicePackage updated = servicePackageRepository.save(servicePackage);
        log.info("Toggled service package [{}] active state to [{}]", updated.getId(), active);
        return ServicePackageDto.fromEntity(updated);
    }

    @Override
    public void deletePackage(Long photographerUserId, Long packageId) {
        if (packageId == null) {
            throw new IllegalArgumentException("Package ID cannot be null.");
        }

        PhotographerProfile profile = requireActivePhotographerProfile(photographerUserId);

        ServicePackage servicePackage = servicePackageRepository.findByIdAndPhotographerProfileId(packageId, profile.getId())
                .orElseThrow(() -> new IllegalArgumentException("Service package not found or does not belong to your profile."));

        servicePackageRepository.delete(servicePackage);
        log.info("Deleted service package [{}] for photographer profile [{}]", packageId, profile.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public ServicePackageDto getPackageById(Long packageId) {
        if (packageId == null) {
            throw new IllegalArgumentException("Package ID cannot be null.");
        }
        return servicePackageRepository.findByIdWithProfile(packageId)
                .map(ServicePackageDto::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("Service package not found with ID: " + packageId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicePackageDto> getActivePackagesForPhotographer(Long photographerProfileId) {
        if (photographerProfileId == null) {
            return List.of();
        }
        List<ServicePackageDto> dtos = servicePackageRepository
                .findByPhotographerProfileIdAndActiveTrueOrderByPriceAsc(photographerProfileId)
                .stream()
                .map(ServicePackageDto::fromEntity)
                .collect(Collectors.toList());

        if (dtos.size() >= 3) {
            dtos.get(1).setRecommended(true);
        } else if (!dtos.isEmpty()) {
            dtos.get(0).setRecommended(true);
        }

        return dtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicePackageDto> getAllPackagesForStudio(Long photographerUserId) {
        PhotographerProfile profile = requireActivePhotographerProfile(photographerUserId);
        return servicePackageRepository.findByPhotographerProfileIdOrderByCreatedAtDesc(profile.getId())
                .stream()
                .map(ServicePackageDto::fromEntity)
                .collect(Collectors.toList());
    }

    private PhotographerProfile requireActivePhotographerProfile(Long userId) {
        if (userId == null) {
            throw new UnauthorizedException("A signed-in photographer is required.");
        }
        PhotographerProfile profile = photographerProfileRepository.findByUserIdWithUser(userId)
                .orElseThrow(() -> new UnauthorizedException("Photographer profile not found for user: " + userId));

        if (profile.getUser() == null || profile.getUser().getRole() != UserRole.PHOTOGRAPHER || profile.getUser().getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Only active photographers can manage service packages.");
        }
        return profile;
    }

    private void applyPricingSettings(ServicePackage servicePackage, ServicePackageRequest request) {
        servicePackage.setNightSurchargeRate(request.getNightSurchargeRate());
        servicePackage.setIncludedTravelKm(request.getIncludedTravelKm());
        servicePackage.setExcessTravelRate(request.getExcessTravelRate());
        servicePackage.setAdditionalHourFee(request.getAdditionalHourFee());
        servicePackage.setExtraEditedPhotoFee(request.getExtraEditedPhotoFee());
        servicePackage.setAssistantFee(request.getAssistantFee());
    }
}
