package com.photoconnect.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a single portfolio image uploaded by an approved photographer.
 *
 * WHY two URL columns (imageUrl + publicId):
 *   imageUrl  — the Cloudinary HTTPS URL used to render the image in HTML.
 *   publicId  — the Cloudinary asset identifier required to delete or manage the asset
 *               via the Cloudinary API. You cannot reliably derive publicId from the URL.
 *               Storing both at upload time avoids fragile string-parsing later.
 *
 * WHY ManyToOne LAZY and no CascadeType.REMOVE on the parent:
 *   A PortfolioImage belongs to exactly one PhotographerProfile.
 *   Deleting a PortfolioImage must NEVER cascade-delete the PhotographerProfile.
 *   Deleting a PhotographerProfile in the future should cascade-delete its images
 *   (handled by a future TASK or explicit repository cleanup), but that is not done here.
 *
 * WHY no direct cascade from PhotographerProfile to PortfolioImage:
 *   We want explicit control over deletion — the service layer calls Cloudinary
 *   delete first, then DB delete. A blind cascade would skip the Cloudinary step.
 */
@Entity
@Table(name = "portfolio_images")
public class PortfolioImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The photographer profile this image belongs to.
     * LAZY: we control exactly when this is initialized via JOIN FETCH or service-layer access.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "photographer_profile_id", nullable = false)
    private PhotographerProfile photographerProfile;

    /**
     * Cloudinary HTTPS URL — used in &lt;img src&gt; tags.
     * Example: https://res.cloudinary.com/mycloudname/image/upload/v123/photoconnect/portfolio/abc123.jpg
     */
    @Column(name = "image_url", nullable = false, length = 2048)
    private String imageUrl;

    /**
     * Cloudinary asset public ID — used to delete or manage the asset via Cloudinary API.
     * Example: photoconnect/portfolio/abc123
     * NOT exposed on public-facing views.
     */
    @Column(name = "public_id", nullable = false, length = 512)
    private String publicId;

    /**
     * Optional caption provided by the photographer when uploading.
     */
    @Column(length = 500)
    private String caption;

    /**
     * Controls display order in the portfolio grid (lower = shown first).
     * Defaults to 0 — images without explicit order are shown in creation-time order.
     */
    @Column(name = "display_order")
    private Integer displayOrder = 0;

    /**
     * Indicates whether this image is selected as the photographer's portfolio cover.
     * At most one image per photographer profile may have is_cover = true.
     */
    @Column(name = "is_cover", nullable = false)
    private boolean isCover = false;

    /**
     * Controlled photography category for this portfolio image.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 50)
    private PortfolioCategory category;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public PortfolioImage() {
    }

    // ── Getters and Setters ────────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PhotographerProfile getPhotographerProfile() {
        return photographerProfile;
    }

    public void setPhotographerProfile(PhotographerProfile photographerProfile) {
        this.photographerProfile = photographerProfile;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isCover() {
        return isCover;
    }

    public Boolean getIsCover() {
        return isCover;
    }

    public void setCover(boolean cover) {
        this.isCover = cover;
    }

    public void setIsCover(boolean isCover) {
        this.isCover = isCover;
    }

    public PortfolioCategory getCategory() {
        return category;
    }

    public void setCategory(PortfolioCategory category) {
        this.category = category;
    }

    public String getCategoryDisplayName() {
        return category != null ? category.getDisplayName() : "General";
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Cloudinary delivery-transformed URL for grid/card thumbnails (600x750, fill, auto quality/format).
     */
    public String getThumbnailUrl() {
        return buildTransformedUrl(this.imageUrl, "c_fill,w_600,h_750,q_auto,f_auto");
    }

    /**
     * Cloudinary delivery-transformed URL for cover hero banners (1200x600, fill, auto quality/format).
     */
    public String getCoverTransformedUrl() {
        return buildTransformedUrl(this.imageUrl, "c_fill,w_1200,h_600,q_auto,f_auto");
    }

    public static String buildTransformedUrl(String url, String transformation) {
        if (url == null || !url.contains("/upload/")) {
            return url;
        }
        if (url.contains("/upload/c_") || url.contains("/upload/w_")) {
            return url;
        }
        return url.replaceFirst("/upload/", "/upload/" + transformation + "/");
    }
}
