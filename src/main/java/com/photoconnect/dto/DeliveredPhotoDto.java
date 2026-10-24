package com.photoconnect.dto;

import com.photoconnect.entity.DeliveredPhoto;
import java.time.LocalDateTime;

public record DeliveredPhotoDto(
        Long id,
        Long albumId,
        String cloudinaryPublicId,
        String secureUrl,
        String thumbnailUrl,
        String previewUrl,
        String downloadUrl,
        String fileName,
        Long fileSizeBytes,
        LocalDateTime createdAt
) {
    public static DeliveredPhotoDto fromEntity(DeliveredPhoto entity) {
        if (entity == null) {
            return null;
        }
        return new DeliveredPhotoDto(
                entity.getId(),
                entity.getAlbum() != null ? entity.getAlbum().getId() : null,
                entity.getCloudinaryPublicId(),
                entity.getSecureUrl(),
                entity.getThumbnailUrl(),
                entity.getPreviewUrl(),
                entity.getDownloadUrl(),
                entity.getFileName(),
                entity.getFileSizeBytes(),
                entity.getCreatedAt()
        );
    }
}
