package com.photoconnect.dto;

import com.photoconnect.entity.SavedPhotographer;

import java.time.LocalDateTime;

/**
 * Data transfer object representing a saved photographer entry for customer views.
 */
public class SavedPhotographerDto {

    private final Long id;
    private final Long photographerId;
    private final PhotographerPublicDto photographer;
    private final LocalDateTime savedAt;

    public SavedPhotographerDto(Long id, Long photographerId, PhotographerPublicDto photographer, LocalDateTime savedAt) {
        this.id = id;
        this.photographerId = photographerId;
        this.photographer = photographer;
        this.savedAt = savedAt;
    }

    public static SavedPhotographerDto from(SavedPhotographer saved, PhotographerPublicDto photographer) {
        if (saved == null) {
            return null;
        }
        return new SavedPhotographerDto(
                saved.getId(),
                saved.getPhotographerProfile() != null ? saved.getPhotographerProfile().getId() : null,
                photographer,
                saved.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getPhotographerId() {
        return photographerId;
    }

    public PhotographerPublicDto getPhotographer() {
        return photographer;
    }

    public LocalDateTime getSavedAt() {
        return savedAt;
    }
}
