package com.photoconnect.repository;

import com.photoconnect.entity.PortfolioCategory;
import com.photoconnect.entity.ServicePackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicePackageRepository extends JpaRepository<ServicePackage, Long> {

    List<ServicePackage> findByPhotographerProfileIdAndActiveTrueOrderByPriceAsc(Long photographerProfileId);

    List<ServicePackage> findByPhotographerProfileIdOrderByCreatedAtDesc(Long photographerProfileId);

    Optional<ServicePackage> findByIdAndPhotographerProfileId(Long id, Long photographerProfileId);

    List<ServicePackage> findByPhotographerProfileIdAndCategoryAndActiveTrueOrderByPriceAsc(
            Long photographerProfileId, PortfolioCategory category);

    long countByPhotographerProfileIdAndActiveTrue(Long photographerProfileId);

    @Query("SELECT sp FROM ServicePackage sp JOIN FETCH sp.photographerProfile pp WHERE sp.id = :id")
    Optional<ServicePackage> findByIdWithProfile(@Param("id") Long id);
}
