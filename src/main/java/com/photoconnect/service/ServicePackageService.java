package com.photoconnect.service;

import com.photoconnect.dto.ServicePackageDto;
import com.photoconnect.dto.ServicePackageRequest;

import java.util.List;

public interface ServicePackageService {

    ServicePackageDto createPackage(Long photographerUserId, ServicePackageRequest request);

    ServicePackageDto updatePackage(Long photographerUserId, Long packageId, ServicePackageRequest request);

    ServicePackageDto togglePackageActive(Long photographerUserId, Long packageId, boolean active);

    void deletePackage(Long photographerUserId, Long packageId);

    ServicePackageDto getPackageById(Long packageId);

    List<ServicePackageDto> getActivePackagesForPhotographer(Long photographerProfileId);

    List<ServicePackageDto> getAllPackagesForStudio(Long photographerUserId);
}
