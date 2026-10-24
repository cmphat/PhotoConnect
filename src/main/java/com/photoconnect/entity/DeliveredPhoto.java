package com.photoconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivered_photos")
public class DeliveredPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "album_id", nullable = false)
    private DeliveryAlbum album;

    @Column(name = "cloudinary_public_id", nullable = false, length = 255)
    private String cloudinaryPublicId;

    @Column(name = "secure_url", nullable = false, length = 1000)
    private String secureUrl;

    @Column(name = "resource_type", nullable = false, length = 50)
    private String resourceType = "image";

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public DeliveredPhoto() {
    }

    public DeliveredPhoto(DeliveryAlbum album, String cloudinaryPublicId, String secureUrl, String fileName, Long fileSizeBytes) {
        this.album = album;
        this.cloudinaryPublicId = cloudinaryPublicId;
        this.secureUrl = secureUrl;
        this.fileName = fileName;
        this.fileSizeBytes = fileSizeBytes;
        this.resourceType = "image";
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.resourceType == null) {
            this.resourceType = "image";
        }
    }

    public String getThumbnailUrl() {
        return buildTransformedUrl(this.secureUrl, "c_fill,w_500,h_500,q_auto,f_auto");
    }

    public String getPreviewUrl() {
        return buildTransformedUrl(this.secureUrl, "c_limit,w_1600,h_1600,q_auto,f_auto");
    }

    public String getDownloadUrl() {
        return buildTransformedUrl(this.secureUrl, "fl_attachment");
    }

    public static String buildTransformedUrl(String url, String transformation) {
        if (url == null || !url.contains("/upload/")) {
            return url;
        }
        if (url.contains("/upload/c_") || url.contains("/upload/w_") || url.contains("/upload/fl_")) {
            return url;
        }
        return url.replaceFirst("/upload/", "/upload/" + transformation + "/");
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DeliveryAlbum getAlbum() {
        return album;
    }

    public void setAlbum(DeliveryAlbum album) {
        this.album = album;
    }

    public String getCloudinaryPublicId() {
        return cloudinaryPublicId;
    }

    public void setCloudinaryPublicId(String cloudinaryPublicId) {
        this.cloudinaryPublicId = cloudinaryPublicId;
    }

    public String getSecureUrl() {
        return secureUrl;
    }

    public void setSecureUrl(String secureUrl) {
        this.secureUrl = secureUrl;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
