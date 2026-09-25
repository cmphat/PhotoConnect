package com.photoconnect.dto;

import com.photoconnect.entity.PortfolioCategory;
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
 *   id                  — identifier for portfolio management actions
 *   imageUrl            — the HTTPS Cloudinary URL for rendering
 *   caption             — optional photographer-supplied description
 *   category            — controlled photography category
 *   categoryDisplayName — human-readable category name
 *   isCover             — whether this is the chosen portfolio cover
 *   thumbnailUrl        — Cloudinary delivery-transformed URL for optimized card display
 */
public class PortfolioImagePublicDto {

    private final Long id;
    private final String imageUrl;
    private final String caption;
    private final PortfolioCategory category;
    private final String categoryDisplayName;
    private final boolean isCover;
    private final String thumbnailUrl;

    private PortfolioImagePublicDto(Long id, String imageUrl, String caption,
                                    PortfolioCategory category, String categoryDisplayName,
                                    boolean isCover, String thumbnailUrl) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.category = category;
        this.categoryDisplayName = categoryDisplayName;
        this.isCover = isCover;
        this.thumbnailUrl = thumbnailUrl;
    }

    /**
     * Maps a PortfolioImage entity to its public-safe DTO.
     * Call within a @Transactional method to avoid LazyInitializationException.
     */
    public static PortfolioImagePublicDto from(PortfolioImage image) {
        return new PortfolioImagePublicDto(
                image.getId(),
                image.getImageUrl(),
                image.getCaption(),
                image.getCategory(),
                image.getCategoryDisplayName(),
                image.isCover(),
                image.getThumbnailUrl()
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

    public PortfolioCategory getCategory() {
        return category;
    }

    public String getCategoryDisplayName() {
        return categoryDisplayName;
    }

    public boolean isCover() {
        return isCover;
    }

    public Boolean getIsCover() {
        return isCover;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
}
