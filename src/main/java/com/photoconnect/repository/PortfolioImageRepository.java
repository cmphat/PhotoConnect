package com.photoconnect.repository;

import com.photoconnect.entity.PortfolioImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Repository for portfolio image metadata queries.
 *
 * All queries are scoped to a specific photographer profile to prevent
 * cross-profile data leakage.
 */
@Repository
public interface PortfolioImageRepository extends JpaRepository<PortfolioImage, Long> {

    /**
     * Returns all portfolio images for a photographer profile, ordered for consistent display.
     * displayOrder ASC → images with lower order number appear first.
     * createdAt DESC → newest first within the same display order tier.
     *
     * WHY not JOIN FETCH here:
     *   The service maps results to a DTO immediately inside a @Transactional method,
     *   so the photographerProfile relationship is loaded within the session boundary.
     *   Adding JOIN FETCH to a List query creates a Cartesian product risk; we access
     *   only the image fields (id, imageUrl, publicId, caption) in the DTO, not the profile.
     */
    List<PortfolioImage> findByPhotographerProfileIdOrderByDisplayOrderAscCreatedAtDesc(Long profileId);

    /**
     * Ownership-safe lookup: find an image ONLY if it belongs to the specified profile.
     *
     * WHY we pass profileId instead of using findById() + checking in Java:
     *   A single atomic query avoids the TOCTOU (time-of-check time-of-use) window.
     *   If we did findById() then profile.getId().equals(expected), a race or
     *   concurrent request could theoretically exploit the gap.
     */
    Optional<PortfolioImage> findByIdAndPhotographerProfileId(Long id, Long profileId);

    /**
     * Count images for a profile — useful for future portfolio limits.
     */
    long countByPhotographerProfileId(Long profileId);

    /**
     * Finds all images marked as cover for a profile (should be at most 1).
     */
    List<PortfolioImage> findByPhotographerProfileIdAndIsCoverTrue(Long profileId);

    /**
     * Finds the single cover image for a profile if one exists.
     */
    Optional<PortfolioImage> findFirstByPhotographerProfileIdAndIsCoverTrue(Long profileId);

    /**
     * Returns all images for fallback cover determination: displayOrder ASC, then oldest createdAt ASC.
     */
    List<PortfolioImage> findByPhotographerProfileIdOrderByDisplayOrderAscCreatedAtAsc(Long profileId);

    /** Bulk cover/fallback candidates for the small set of profiles shown on a dashboard. */
    @Query("""
            SELECT i FROM PortfolioImage i
            JOIN FETCH i.photographerProfile p
            WHERE p.id IN :profileIds
            ORDER BY p.id ASC,
                     CASE WHEN i.isCover = true THEN 0 ELSE 1 END ASC,
                     i.displayOrder ASC,
                     i.createdAt ASC
            """)
    List<PortfolioImage> findDashboardImageCandidates(@Param("profileIds") List<Long> profileIds);

    @Query("""
            SELECT COUNT(DISTINCT i.category) FROM PortfolioImage i
            WHERE i.photographerProfile.id = :profileId
              AND i.category IS NOT NULL
            """)
    long countDistinctCategoriesByProfileId(@Param("profileId") Long profileId);

    @Query("""
            SELECT i FROM PortfolioImage i
            WHERE i.photographerProfile.id = :profileId
            ORDER BY CASE WHEN i.isCover = true THEN 0 ELSE 1 END,
                     i.displayOrder ASC,
                     i.createdAt DESC,
                     i.id DESC
            """)
    List<PortfolioImage> findStudioPreview(@Param("profileId") Long profileId, Pageable pageable);
}
