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
    private final PhotographerProfileService photographerProfileService;
    private final com.photoconnect.repository.BookingRepository bookingRepository;
    private final ScheduleService scheduleService;

    public PublicPhotographerServiceImpl(PhotographerProfileRepository photographerProfileRepository,
                                         @Autowired(required = false) PortfolioImageRepository portfolioImageRepository) {
        this(photographerProfileRepository, portfolioImageRepository, null, null, null);
    }

    public PublicPhotographerServiceImpl(PhotographerProfileRepository photographerProfileRepository,
                                         @Autowired(required = false) PortfolioImageRepository portfolioImageRepository,
                                         @Autowired(required = false) PhotographerProfileService photographerProfileService) {
        this(photographerProfileRepository, portfolioImageRepository, photographerProfileService, null, null);
    }

    @Autowired
    public PublicPhotographerServiceImpl(PhotographerProfileRepository photographerProfileRepository,
                                         @Autowired(required = false) PortfolioImageRepository portfolioImageRepository,
                                         @Autowired(required = false) PhotographerProfileService photographerProfileService,
                                         @Autowired(required = false) com.photoconnect.repository.BookingRepository bookingRepository,
                                         @Autowired(required = false) ScheduleService scheduleService) {
        this.photographerProfileRepository = photographerProfileRepository;
        this.portfolioImageRepository = portfolioImageRepository;
        this.photographerProfileService = photographerProfileService;
        this.bookingRepository = bookingRepository;
        this.scheduleService = scheduleService;
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
        String category = request != null ? request.getNormalizedCategory() : null;
        Double minRating = request != null ? request.getMinRating() : null;
        java.time.LocalDate availableDate = request != null ? request.getAvailableDate() : null;
        String sortBy = request != null ? request.getEffectiveSortBy() : "RECOMMENDED";

        List<PhotographerProfile> approved = photographerProfileRepository.searchApprovedPhotographers(
                PhotographerVerificationStatus.APPROVED,
                keyword,
                city,
                minPrice,
                maxPrice,
                minExperience
        );

        return approved.stream()
                .map(p -> toPublicDtoWithRecommendation(p, availableDate))
                .filter(dto -> filterByCategory(dto, category))
                .filter(dto -> filterByMinRating(dto, minRating))
                .filter(dto -> filterByAvailability(dto, availableDate))
                .sorted(getComparator(sortBy))
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

        if (request == null || !request.hasAdvancedFiltersOrSort()) {
            String keyword = request != null ? request.getNormalizedKeyword() : null;
            String city = request != null ? request.getNormalizedCity() : null;
            BigDecimal minPrice = request != null ? request.getMinPrice() : null;
            BigDecimal maxPrice = request != null ? request.getMaxPrice() : null;
            Integer minExperience = request != null ? request.getMinExperience() : null;

            return photographerProfileRepository.searchApprovedPhotographersPaged(
                            PhotographerVerificationStatus.APPROVED,
                            keyword, city, minPrice, maxPrice, minExperience, pageable)
                    .map(p -> toPublicDtoWithRecommendation(p, null));
        }

        List<PhotographerPublicDto> allMatches = searchPhotographers(request);
        int total = allMatches.size();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), total);

        List<PhotographerPublicDto> pageContent = (start < total)
                ? allMatches.subList(start, end)
                : java.util.Collections.emptyList();

        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, total);
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
        return toPublicDtoWithRecommendation(profile, null);
    }

    private PhotographerPublicDto toPublicDtoWithRecommendation(PhotographerProfile profile, java.time.LocalDate requestedDate) {
        String coverImageUrl = null;
        int portfolioCount = 0;
        boolean hasCover = false;
        if (portfolioImageRepository != null && profile.getId() != null) {
            List<PortfolioImage> images = portfolioImageRepository
                    .findByPhotographerProfileIdOrderByDisplayOrderAscCreatedAtDesc(profile.getId());
            portfolioCount = images.size();
            if (!images.isEmpty()) {
                for (PortfolioImage img : images) {
                    if (img.isCover()) {
                        coverImageUrl = img.getImageUrl();
                        hasCover = true;
                        break;
                    }
                }
                if (coverImageUrl == null) {
                    coverImageUrl = images.get(0).getImageUrl();
                }
            }
        }
        Integer completeness = null;
        if (photographerProfileService != null) {
            completeness = photographerProfileService.calculateProfileCompleteness(profile, portfolioCount, hasCover);
        }

        int completedBookings = 0;
        if (bookingRepository != null && profile.getId() != null) {
            completedBookings = (int) bookingRepository.countCompletedBookingsForPhotographer(profile.getId());
        }

        Boolean availableOnDate = null;
        if (requestedDate != null) {
            if (scheduleService != null && profile.getId() != null) {
                availableOnDate = scheduleService.isDateAvailable(profile.getId(), requestedDate);
            } else {
                availableOnDate = true;
            }
        }

        double score = calculateRecommendationScore(profile, completedBookings, requestedDate, completeness, availableOnDate);

        return PhotographerPublicDto.from(profile, coverImageUrl, completeness, score, completedBookings, availableOnDate);
    }

    private double calculateRecommendationScore(PhotographerProfile profile,
                                               int completedBookings,
                                               java.time.LocalDate requestedDate,
                                               Integer completeness,
                                               Boolean availableOnDate) {
        double rating = profile.getAverageRating() != null ? profile.getAverageRating() : 0.0;
        int reviews = profile.getReviewCount() != null ? profile.getReviewCount() : 0;

        double normRating = Math.min(Math.max(rating / 5.0, 0.0), 1.0);
        double normReviews = Math.min(reviews / 20.0, 1.0);
        double normCompleted = Math.min(completedBookings / 15.0, 1.0);

        double availabilityScore = 1.0;
        if (requestedDate != null) {
            availabilityScore = (availableOnDate != null && availableOnDate) ? 1.0 : 0.0;
        }

        // Composite scoring formula: (0.35 * rating) + (0.25 * reviews) + (0.25 * completed) + (0.15 * availability)
        double score = (0.35 * normRating) + (0.25 * normReviews) + (0.25 * normCompleted) + (0.15 * availabilityScore);

        // Cold-start protection: Give newly approved photographers with 0 reviews an exploration boost (0.15)
        // ensuring high-potential new talent is not pushed to the bottom of recommendations
        if (reviews == 0) {
            score += 0.15;
        }

        if (completeness != null && completeness >= 80) {
            score += 0.05;
        }
        if (profile.isTravelAvailable()) {
            score += 0.02;
        }

        return Math.min(Math.max(Math.round(score * 100.0) / 100.0, 0.0), 1.0);
    }

    private boolean filterByCategory(PhotographerPublicDto dto, String category) {
        if (category == null || category.isBlank()) {
            return true;
        }
        String catLower = category.trim().toLowerCase();
        if (dto.getSpecialties() != null && dto.getSpecialties().toLowerCase().contains(catLower)) {
            return true;
        }
        for (String name : dto.getSpecialtyDisplayNames()) {
            if (name.toLowerCase().contains(catLower)) {
                return true;
            }
        }
        return false;
    }

    private boolean filterByMinRating(PhotographerPublicDto dto, Double minRating) {
        if (minRating == null) {
            return true;
        }
        Double avgRating = dto.getAverageRating();
        return avgRating != null && avgRating >= minRating;
    }

    private boolean filterByAvailability(PhotographerPublicDto dto, java.time.LocalDate availableDate) {
        if (availableDate == null) {
            return true;
        }
        return dto.getAvailableOnRequestedDate() == null || dto.getAvailableOnRequestedDate();
    }

    private java.util.Comparator<PhotographerPublicDto> getComparator(String sortBy) {
        if (sortBy == null || sortBy.isBlank() || "RECOMMENDED".equalsIgnoreCase(sortBy)) {
            return java.util.Comparator.comparing(
                    PhotographerPublicDto::getRecommendationScore,
                    java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())
            ).thenComparing(
                    PhotographerPublicDto::getAverageRating,
                    java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())
            ).thenComparing(
                    PhotographerPublicDto::getReviewCount,
                    java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())
            ).thenComparing(
                    PhotographerPublicDto::getId,
                    java.util.Comparator.reverseOrder()
            );
        }

        switch (sortBy.toUpperCase()) {
            case "RATING_DESC":
                return java.util.Comparator.comparing(
                        PhotographerPublicDto::getAverageRating,
                        java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())
                ).thenComparing(
                        PhotographerPublicDto::getReviewCount,
                        java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())
                ).thenComparing(
                        PhotographerPublicDto::getId,
                        java.util.Comparator.reverseOrder()
                );
            case "PRICE_ASC":
                return java.util.Comparator.comparing(
                        PhotographerPublicDto::getPriceFrom,
                        java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())
                ).thenComparing(
                        PhotographerPublicDto::getAverageRating,
                        java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())
                );
            case "PRICE_DESC":
                return java.util.Comparator.comparing(
                        PhotographerPublicDto::getPriceFrom,
                        java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())
                ).thenComparing(
                        PhotographerPublicDto::getAverageRating,
                        java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())
                );
            case "REVIEWS_DESC":
                return java.util.Comparator.comparing(
                        PhotographerPublicDto::getReviewCount,
                        java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())
                ).thenComparing(
                        PhotographerPublicDto::getAverageRating,
                        java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())
                );
            case "COMPLETED_DESC":
                return java.util.Comparator.comparing(
                        PhotographerPublicDto::getCompletedBookingsCount,
                        java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())
                ).thenComparing(
                        PhotographerPublicDto::getAverageRating,
                        java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())
                );
            case "NEWEST":
                return java.util.Comparator.comparing(
                        PhotographerPublicDto::getCreatedAt,
                        java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())
                );
            default:
                return java.util.Comparator.comparing(
                        PhotographerPublicDto::getRecommendationScore,
                        java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())
                );
        }
    }
}
