package com.photoconnect.repository;

import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerVerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhotographerProfileRepository extends JpaRepository<PhotographerProfile, Long> {

    Optional<PhotographerProfile> findByUserId(Long userId);

    @Query("""
            SELECT p FROM PhotographerProfile p
            JOIN FETCH p.user
            WHERE p.user.id = :userId
            """)
    Optional<PhotographerProfile> findByUserIdWithUser(@Param("userId") Long userId);

    boolean existsByUserId(Long userId);

    Optional<PhotographerProfile> findByUserEmail(String email);

    long countByVerificationStatus(PhotographerVerificationStatus status);

    @Query("""
            SELECT p FROM PhotographerProfile p
            JOIN FETCH p.user
            ORDER BY p.createdAt DESC, p.id DESC
            """)
    List<PhotographerProfile> findAllWithUser();

    // ── Admin queries: JOIN FETCH ensures the User proxy is initialized
    // within the Hibernate session so the JSP can safely access user fields
    // after the transaction closes (open-in-view=false).

    @Query("""
            SELECT p FROM PhotographerProfile p
            JOIN FETCH p.user
            WHERE p.verificationStatus = :status
            ORDER BY p.createdAt ASC
            """)
    List<PhotographerProfile> findByVerificationStatusWithUser(
            @Param("status") PhotographerVerificationStatus status);

    @Query("""
            SELECT p FROM PhotographerProfile p
            JOIN FETCH p.user
            WHERE p.id = :id
            """)
    Optional<PhotographerProfile> findByIdWithUser(@Param("id") Long id);

    // ── Public marketplace query: approved profile by id ──────────────────
    // Used by the public PhotographerController so that PENDING/REJECTED/
    // SUSPENDED profiles are NEVER accessible via a direct URL guess.
    // The status filter is in JPQL, not JSP, making it server-enforced.
    @Query("""
            SELECT p FROM PhotographerProfile p
            JOIN FETCH p.user
            WHERE p.id = :id
            AND p.verificationStatus = :status
            """)
    Optional<PhotographerProfile> findByIdAndVerificationStatusWithUser(
            @Param("id") Long id,
            @Param("status") PhotographerVerificationStatus status);

    // ── Public marketplace query: search and filter approved photographers ─
    @Query("""
            SELECT p FROM PhotographerProfile p
            JOIN FETCH p.user
            WHERE p.verificationStatus = :status
            AND (:keyword IS NULL OR :keyword = ''
                 OR LOWER(p.displayName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR (p.headline IS NOT NULL AND LOWER(p.headline) LIKE LOWER(CONCAT('%', :keyword, '%')))
                 OR (p.specialties IS NOT NULL AND LOWER(p.specialties) LIKE LOWER(CONCAT('%', :keyword, '%')))
                 OR (p.bio IS NOT NULL AND LOWER(p.bio) LIKE LOWER(CONCAT('%', :keyword, '%')))
                 OR (p.city IS NOT NULL AND LOWER(p.city) LIKE LOWER(CONCAT('%', :keyword, '%'))))
            AND (:city IS NULL OR :city = ''
                 OR (p.city IS NOT NULL AND LOWER(p.city) LIKE LOWER(CONCAT('%', :city, '%'))))
            AND (:minPrice IS NULL OR (p.priceFrom IS NOT NULL AND p.priceFrom >= :minPrice))
            AND (:maxPrice IS NULL OR (p.priceFrom IS NOT NULL AND p.priceFrom <= :maxPrice))
            AND (:minExperience IS NULL OR (p.experienceYears IS NOT NULL AND p.experienceYears >= :minExperience))
            ORDER BY p.createdAt DESC, p.id DESC
            """)
    List<PhotographerProfile> searchApprovedPhotographers(
            @Param("status") PhotographerVerificationStatus status,
            @Param("keyword") String keyword,
            @Param("city") String city,
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            @Param("minExperience") Integer minExperience);

    @Query(value = """
            SELECT p FROM PhotographerProfile p
            JOIN FETCH p.user
            WHERE p.verificationStatus = :status
            AND (:keyword IS NULL OR :keyword = ''
                 OR LOWER(p.displayName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR (p.headline IS NOT NULL AND LOWER(p.headline) LIKE LOWER(CONCAT('%', :keyword, '%')))
                 OR (p.specialties IS NOT NULL AND LOWER(p.specialties) LIKE LOWER(CONCAT('%', :keyword, '%')))
                 OR (p.bio IS NOT NULL AND LOWER(p.bio) LIKE LOWER(CONCAT('%', :keyword, '%')))
                 OR (p.city IS NOT NULL AND LOWER(p.city) LIKE LOWER(CONCAT('%', :keyword, '%'))))
            AND (:city IS NULL OR :city = ''
                 OR (p.city IS NOT NULL AND LOWER(p.city) LIKE LOWER(CONCAT('%', :city, '%'))))
            AND (:minPrice IS NULL OR (p.priceFrom IS NOT NULL AND p.priceFrom >= :minPrice))
            AND (:maxPrice IS NULL OR (p.priceFrom IS NOT NULL AND p.priceFrom <= :maxPrice))
            AND (:minExperience IS NULL OR (p.experienceYears IS NOT NULL AND p.experienceYears >= :minExperience))
            ORDER BY p.createdAt DESC, p.id DESC
            """,
            countQuery = """
            SELECT COUNT(p) FROM PhotographerProfile p
            WHERE p.verificationStatus = :status
            AND (:keyword IS NULL OR :keyword = ''
                 OR LOWER(p.displayName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR (p.headline IS NOT NULL AND LOWER(p.headline) LIKE LOWER(CONCAT('%', :keyword, '%')))
                 OR (p.specialties IS NOT NULL AND LOWER(p.specialties) LIKE LOWER(CONCAT('%', :keyword, '%')))
                 OR (p.bio IS NOT NULL AND LOWER(p.bio) LIKE LOWER(CONCAT('%', :keyword, '%')))
                 OR (p.city IS NOT NULL AND LOWER(p.city) LIKE LOWER(CONCAT('%', :keyword, '%'))))
            AND (:city IS NULL OR :city = ''
                 OR (p.city IS NOT NULL AND LOWER(p.city) LIKE LOWER(CONCAT('%', :city, '%'))))
            AND (:minPrice IS NULL OR (p.priceFrom IS NOT NULL AND p.priceFrom >= :minPrice))
            AND (:maxPrice IS NULL OR (p.priceFrom IS NOT NULL AND p.priceFrom <= :maxPrice))
            AND (:minExperience IS NULL OR (p.experienceYears IS NOT NULL AND p.experienceYears >= :minExperience))
            """)
    Page<PhotographerProfile> searchApprovedPhotographersPaged(
            @Param("status") PhotographerVerificationStatus status,
            @Param("keyword") String keyword,
            @Param("city") String city,
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            @Param("minExperience") Integer minExperience,
            Pageable pageable);
}
