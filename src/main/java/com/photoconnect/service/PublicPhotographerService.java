package com.photoconnect.service;

import com.photoconnect.dto.PhotographerPublicDto;

import java.util.List;

/**
 * Public read-only service for the photographer marketplace.
 * Returns only APPROVED profiles as public-safe DTOs.
 * No authentication is required to call these methods.
 */
public interface PublicPhotographerService {

    /**
     * Returns all approved photographer profiles as public DTOs,
     * ordered newest-first.
     * Profiles with PENDING, REJECTED, or SUSPENDED status are excluded.
     */
    List<PhotographerPublicDto> listApprovedPhotographers();

    /**
     * Searches and filters approved photographer profiles based on the search request criteria.
     * Profiles with PENDING, REJECTED, or SUSPENDED status are excluded.
     *
     * @param request search and filter criteria
     * @return filtered list of public photographer DTOs
     * @throws IllegalArgumentException if validation constraints on price or experience are violated
     */
    List<PhotographerPublicDto> searchPhotographers(com.photoconnect.dto.PhotographerSearchRequest request);

    /**
     * Returns the approved photographer by id as a public DTO.
     *
     * @throws IllegalArgumentException if no profile exists with the given id,
     *                                  or if the profile is not APPROVED.
     */
    PhotographerPublicDto getApprovedPhotographerById(Long id);
}
