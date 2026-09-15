package com.photoconnect.service;

import com.photoconnect.dto.PhotographerPublicDto;
import com.photoconnect.dto.PhotographerSearchRequest;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.PortfolioImage;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.repository.PortfolioImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PublicPhotographerServiceImpl implements PublicPhotographerService {

    private final PhotographerProfileRepository photographerProfileRepository;
    private final PortfolioImageRepository portfolioImageRepository;

    public PublicPhotographerServiceImpl(PhotographerProfileRepository photographerProfileRepository,
                                         @Autowired(required = false) PortfolioImageRepository portfolioImageRepository) {
        this.photographerProfileRepository = photographerProfileRepository;
        this.portfolioImageRepository = portfolioImageRepository;
    }

    /**
     * Returns all APPROVED profiles as public DTOs.
     */
    @Override
    @Transactional(readOnly = true)
    public List<PhotographerPublicDto> listApprovedPhotographers() {
        return searchPhotographers(null);
    }

    /**
     * Searches and filters APPROVED profiles.
     * Enforces validation constraints on price range and non-negative values.
     */
    @Override
    @Transactional(readOnly = true)
    public List<PhotographerPublicDto> searchPhotographers(PhotographerSearchRequest request) {
        if (request != null && !request.isValid()) {
            throw new IllegalArgumentException(request.getValidationError());
        }

        String keyword = request != null ? request.getNormalizedKeyword() : null;
        String city = request != null ? request.getNormalizedCity() : null;
        BigDecimal minPrice = request != null ? request.getMinPrice() : null;
        BigDecimal maxPrice = request != null ? request.getMaxPrice() : null;
        Integer minExperience = request != null ? request.getMinExperience() : null;

        List<PhotographerProfile> approved = photographerProfileRepository.searchApprovedPhotographers(
                PhotographerVerificationStatus.APPROVED,
                keyword,
                city,
                minPrice,
                maxPrice,
                minExperience
        );

        return approved.stream()
                .map(this::toPublicDtoWithCover)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PhotographerPublicDto> searchPhotographers(PhotographerSearchRequest request, Pageable pageable) {
        if (request != null && !request.isValid()) {
            throw new IllegalArgumentException(request.getValidationError());
        }
        if (pageable == null || !pageable.isPaged() || pageable.getPageSize() < 1 || pageable.getPageSize() > 24) {
            throw new IllegalArgumentException("A page size between 1 and 24 is required.");
        }

        String keyword = request != null ? request.getNormalizedKeyword() : null;
        String city = request != null ? request.getNormalizedCity() : null;
        BigDecimal minPrice = request != null ? request.getMinPrice() : null;
        BigDecimal maxPrice = request != null ? request.getMaxPrice() : null;
        Integer minExperience = request != null ? request.getMinExperience() : null;

        return photographerProfileRepository.searchApprovedPhotographersPaged(
                        PhotographerVerificationStatus.APPROVED,
                        keyword, city, minPrice, maxPrice, minExperience, pageable)
                .map(this::toPublicDtoWithCover);
    }

    /**
     * Returns a single APPROVED profile as a public DTO.
     *
     * @throws IllegalArgumentException if the profile does not exist or is not APPROVED.
     */
    @Override
    @Transactional(readOnly = true)
    public PhotographerPublicDto getApprovedPhotographerById(Long id) {
        PhotographerProfile profile = photographerProfileRepository
                .findByIdAndVerificationStatusWithUser(id, PhotographerVerificationStatus.APPROVED)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Approved photographer not found with id: " + id));
        return toPublicDtoWithCover(profile);
    }

    private PhotographerPublicDto toPublicDtoWithCover(PhotographerProfile profile) {
        String coverImageUrl = null;
        if (portfolioImageRepository != null && profile.getId() != null) {
            List<PortfolioImage> images = portfolioImageRepository
                    .findByPhotographerProfileIdOrderByDisplayOrderAscCreatedAtDesc(profile.getId());
            if (!images.isEmpty()) {
                coverImageUrl = images.get(0).getImageUrl();
            }
        }
        return PhotographerPublicDto.from(profile, coverImageUrl);
    }
}
