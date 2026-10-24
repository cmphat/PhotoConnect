package com.photoconnect.repository;

import com.photoconnect.entity.DeliveredPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveredPhotoRepository extends JpaRepository<DeliveredPhoto, Long> {

    List<DeliveredPhoto> findByAlbumIdOrderByCreatedAtAsc(Long albumId);

    Optional<DeliveredPhoto> findByIdAndAlbumId(Long photoId, Long albumId);

    long countByAlbumId(Long albumId);
}
