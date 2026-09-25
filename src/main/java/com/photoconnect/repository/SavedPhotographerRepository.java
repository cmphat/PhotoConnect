package com.photoconnect.repository;

import com.photoconnect.entity.SavedPhotographer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedPhotographerRepository extends JpaRepository<SavedPhotographer, Long> {

    boolean existsByCustomerIdAndPhotographerProfileId(Long customerId, Long photographerProfileId);

    Optional<SavedPhotographer> findByCustomerIdAndPhotographerProfileId(Long customerId, Long photographerProfileId);

    List<SavedPhotographer> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    long countByCustomerId(Long customerId);

    void deleteByCustomerIdAndPhotographerProfileId(Long customerId, Long photographerProfileId);

    /**
     * Retrieves saved photographers for a customer where the photographer is currently APPROVED,
     * fetching the profile and user entities to avoid N+1 queries.
     */
    @Query("SELECT s FROM SavedPhotographer s " +
           "JOIN FETCH s.photographerProfile p " +
           "JOIN FETCH p.user u " +
           "WHERE s.customer.id = :customerId " +
           "AND p.verificationStatus = com.photoconnect.entity.PhotographerVerificationStatus.APPROVED " +
           "ORDER BY s.createdAt DESC")
    List<SavedPhotographer> findApprovedByCustomerIdWithProfile(@Param("customerId") Long customerId);

    /**
     * Retrieves only the photographer profile IDs saved by a customer.
     */
    @Query("SELECT s.photographerProfile.id FROM SavedPhotographer s WHERE s.customer.id = :customerId")
    List<Long> findSavedPhotographerProfileIdsByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT s FROM SavedPhotographer s " +
           "JOIN FETCH s.photographerProfile p " +
           "JOIN FETCH p.user u " +
           "WHERE s.customer.id = :customerId " +
           "AND p.verificationStatus = com.photoconnect.entity.PhotographerVerificationStatus.APPROVED " +
           "ORDER BY s.createdAt DESC")
    List<SavedPhotographer> findApprovedPreviewByCustomerId(@Param("customerId") Long customerId,
                                                            Pageable pageable);

    @Query("SELECT COUNT(s) FROM SavedPhotographer s " +
           "WHERE s.customer.id = :customerId " +
           "AND s.photographerProfile.verificationStatus = com.photoconnect.entity.PhotographerVerificationStatus.APPROVED")
    long countApprovedByCustomerId(@Param("customerId") Long customerId);
}
