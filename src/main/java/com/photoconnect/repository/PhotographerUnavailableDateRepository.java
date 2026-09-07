package com.photoconnect.repository;

import com.photoconnect.entity.PhotographerUnavailableDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PhotographerUnavailableDateRepository extends JpaRepository<PhotographerUnavailableDate, Long> {

    List<PhotographerUnavailableDate> findByPhotographerProfileIdOrderByDateAsc(Long profileId);

    boolean existsByPhotographerProfileIdAndDate(Long profileId, LocalDate date);

    void deleteByIdAndPhotographerProfileId(Long id, Long profileId);
}
