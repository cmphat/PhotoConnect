package com.photoconnect.dto;

import com.photoconnect.entity.PortfolioImage;

/**
 * Public-safe DTO for portfolio images shown on the public photographer detail page.
 *
 * WHY publicId is excluded:
 *   publicId is Cloudinary's internal asset identifier — needed to delete/manage assets
 *   via the Cloudinary API. Exposing it publicly would allow anyone to extract the
 *   Cloudinary path and potentially use it in API calls. It is strictly internal metadata.
 *
 * Fields included:
 *   id        — needed for delete actions on the PRIVATE portfolio management page
 *               (re-used intentionally; delete is access-controlled server-side)
 *   imageUrl  — the HTTPS Cloudinary URL for rendering in an <img> tag
 *   caption   — optional photographer-supplied description
 */
public class PortfolioImagePublicDto {

    private final Long id;
    private final String imageUrl;
    private final String caption;

    private PortfolioImagePublicDto(Long id, String imageUrl, String caption) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.caption = caption;
    }

    /**
     * Maps a PortfolioImage entity to its public-safe DTO.
     * Call within a @Transactional method to avoid LazyInitializationException.
     */
    public static PortfolioImagePublicDto from(PortfolioImage image) {
        return new PortfolioImagePublicDto(
                image.getId(),
                image.getImageUrl(),
                image.getCaption()
        );
    }

    public Long getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCaption() {
        return caption;
    }
}
