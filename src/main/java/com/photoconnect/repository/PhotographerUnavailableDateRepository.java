package com.photoconnect.repository;

import com.photoconnect.entity.PhotographerUnavailableDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PhotographerUnavailableDateRepository extends JpaRepository<PhotographerUnavailableDate, Long> {

    List<PhotographerUnavailableDate> findByPhotographerProfileIdOrderByDateAsc(Long profileId);

    boolean existsByPhotographerProfileIdAndDate(Long profileId, LocalDate date);

    void deleteByIdAndPhotographerProfileId(Long id, Long profileId);

    long countByPhotographerProfileIdAndDateGreaterThanEqual(Long profileId, LocalDate date);

    Optional<PhotographerUnavailableDate> findFirstByPhotographerProfileIdAndDateGreaterThanEqualOrderByDateAsc(
            Long profileId, LocalDate date);
}
