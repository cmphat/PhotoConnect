package com.photoconnect.service;

import com.photoconnect.dto.PortfolioImagePublicDto;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.PortfolioCategory;
import com.photoconnect.entity.PortfolioImage;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.repository.PortfolioImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * Implementation of PortfolioService.
 *
 * PARTIAL FAILURE STRATEGY (Upload):
 *   If Cloudinary upload succeeds but DB save fails:
 *   - We attempt to delete the orphaned Cloudinary asset in a catch block.
 *   - If that rollback deletion also fails, the orphan is logged as a warning.
 *   - This is the documented limitation for this course project.
 *   - Full distributed-transaction infrastructure (saga, outbox pattern) is out of scope.
 *
 * PARTIAL FAILURE STRATEGY (Delete):
 *   Cloudinary deletion runs first.
 *   If Cloudinary deletion fails, we throw — the DB record is NOT deleted.
 *   This keeps the DB record as a "source of truth" for what the user still owns,
 *   and prevents silent data inconsistency where the URL points to a deleted asset.
 *
 * OWNERSHIP:
 *   All operations derive the photographer profile from the SESSION userId.
 *   Controller MUST NOT pass profileId from form inputs — that would allow
 *   a malicious user to supply another photographer's profileId.
 */
@Service
public class PortfolioServiceImpl implements PortfolioService {

    private static final Logger log = LoggerFactory.getLogger(PortfolioServiceImpl.class);

    private static final String CLOUDINARY_FOLDER = "photoconnect/portfolio";
    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024; // 10 MB
    private static final int MAX_CAPTION_LENGTH = 500;

    /**
     * Allowed MIME types. We check content-type (not just file extension)
     * to reject files with misleading extensions.
     */
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final PhotographerProfileRepository photographerProfileRepository;
    private final PortfolioImageRepository portfolioImageRepository;
    private final CloudinaryStorageService cloudinaryStorageService;

    public PortfolioServiceImpl(PhotographerProfileRepository photographerProfileRepository,
                                PortfolioImageRepository portfolioImageRepository,
                                CloudinaryStorageService cloudinaryStorageService) {
        this.photographerProfileRepository = photographerProfileRepository;
        this.portfolioImageRepository = portfolioImageRepository;
        this.cloudinaryStorageService = cloudinaryStorageService;
    }

    // ── Upload ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PortfolioImage addPortfolioImage(Long userId, MultipartFile file, String caption, PortfolioCategory category) {
        if (userId == null) {
            throw new IllegalArgumentException("Authenticated photographer is required.");
        }
        if (category == null) {
            throw new IllegalArgumentException("Portfolio category is required.");
        }

        // 1. Load photographer profile for authenticated user
        PhotographerProfile profile = photographerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "No photographer profile found for user id: " + userId));

        // 2. Require APPROVED status — PENDING/REJECTED/SUSPENDED cannot upload
        if (profile.getVerificationStatus() != PhotographerVerificationStatus.APPROVED) {
            throw new IllegalStateException(
                    "Only APPROVED photographers can upload portfolio images. " +
                    "Current status: " + profile.getVerificationStatus());
        }

        // 3. Validate the uploaded file
        validateFile(file);

        String normalizedCaption = caption != null ? caption.trim() : null;
        if (normalizedCaption != null && normalizedCaption.length() > MAX_CAPTION_LENGTH) {
            throw new IllegalArgumentException("Caption cannot exceed 500 characters.");
        }
        if (normalizedCaption != null && normalizedCaption.isEmpty()) {
            normalizedCaption = null;
        }

        // 4. Automatic cover: if photographer has 0 images, this first image becomes cover
        long existingCount = portfolioImageRepository.countByPhotographerProfileId(profile.getId());
        boolean shouldBeCover = (existingCount == 0);

        // 5. Upload to Cloudinary — binary storage
        CloudinaryStorageService.CloudinaryUploadResult uploadResult =
                cloudinaryStorageService.uploadImage(file, CLOUDINARY_FOLDER);

        // 6. Persist metadata in SQL Server
        //    If this save fails, attempt to roll back the Cloudinary upload.
        PortfolioImage image = new PortfolioImage();
        image.setPhotographerProfile(profile);
        image.setImageUrl(uploadResult.secureUrl());
        image.setPublicId(uploadResult.publicId());
        image.setCaption(normalizedCaption);
        image.setCategory(category);
        image.setCover(shouldBeCover);
        image.setDisplayOrder(0);

        try {
            return portfolioImageRepository.save(image);
        } catch (Exception dbException) {
            // DB save failed — attempt Cloudinary rollback to avoid orphaned asset
            log.warn("DB save failed after successful Cloudinary upload. " +
                     "Attempting Cloudinary rollback for publicId [{}]", uploadResult.publicId());
            try {
                cloudinaryStorageService.deleteImage(uploadResult.publicId());
                log.info("Cloudinary rollback successful for publicId [{}]", uploadResult.publicId());
            } catch (Exception cloudinaryRollbackException) {
                // Rollback also failed — log the orphan for manual cleanup
                log.error("ORPHANED CLOUDINARY ASSET: publicId [{}] — DB save and rollback both failed. " +
                          "Manual cleanup required.", uploadResult.publicId(), cloudinaryRollbackException);
            }
            throw new RuntimeException("Failed to save portfolio image to database. Cloudinary asset may have been rolled back.", dbException);
        }
    }

    @Override
    @Transactional
    public PortfolioImage addPortfolioImage(Long userId, MultipartFile file, String caption) {
        return addPortfolioImage(userId, file, caption, PortfolioCategory.OTHER);
    }

    // ── Cover Selection ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public void setCoverImage(Long userId, Long imageId) {
        if (userId == null) {
            throw new IllegalArgumentException("Authenticated photographer is required.");
        }
        if (imageId == null) {
            throw new IllegalArgumentException("Image ID is required.");
        }

        PhotographerProfile profile = photographerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "No photographer profile found for user id: " + userId));

        if (profile.getVerificationStatus() != PhotographerVerificationStatus.APPROVED) {
            throw new IllegalStateException(
                    "Only APPROVED photographers can manage portfolio cover images. " +
                    "Current status: " + profile.getVerificationStatus());
        }

        PortfolioImage targetImage = portfolioImageRepository
                .findByIdAndPhotographerProfileId(imageId, profile.getId())
                .orElseThrow(() -> new SecurityException(
                        "Image id [" + imageId + "] does not belong to photographer profile [" + profile.getId() + "]"));

        if (targetImage.isCover()) {
            return; // Already cover, idempotent
        }

        // Unset any current cover images for this profile to guarantee single-cover invariant
        List<PortfolioImage> currentCovers = portfolioImageRepository
                .findByPhotographerProfileIdAndIsCoverTrue(profile.getId());
        for (PortfolioImage existingCover : currentCovers) {
            if (!existingCover.getId().equals(targetImage.getId())) {
                existingCover.setCover(false);
                portfolioImageRepository.save(existingCover);
            }
        }

        targetImage.setCover(true);
        portfolioImageRepository.save(targetImage);
        log.info("Set image [{}] as cover for profile [{}]", imageId, profile.getId());
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deletePortfolioImage(Long userId, Long imageId) {
        // 1. Load this user's photographer profile
        PhotographerProfile profile = photographerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "No photographer profile found for user id: " + userId));

        // 2. Load the image, scoped to this profile's id — ownership enforced at DB level
        PortfolioImage image = portfolioImageRepository
                .findByIdAndPhotographerProfileId(imageId, profile.getId())
                .orElseThrow(() -> new SecurityException(
                        "Image id [" + imageId + "] does not belong to photographer profile [" + profile.getId() + "]"));

        // 3. Delete from Cloudinary first
        //    If this fails, we throw and the DB record is NOT deleted.
        //    This prevents the DB from losing track of an asset that still exists in Cloudinary.
        String publicId = image.getPublicId();
        boolean wasCover = image.isCover();

        cloudinaryStorageService.deleteImage(publicId);

        // 4. Cloudinary deletion succeeded — now remove the DB record
        portfolioImageRepository.delete(image);
        portfolioImageRepository.flush();

        log.info("Deleted portfolio image [{}] with Cloudinary publicId [{}] for profile [{}]",
                imageId, publicId, profile.getId());

        // 5. If the deleted image was the cover, assign deterministic fallback cover
        if (wasCover) {
            List<PortfolioImage> remaining = portfolioImageRepository
                    .findByPhotographerProfileIdOrderByDisplayOrderAscCreatedAtAsc(profile.getId());
            if (!remaining.isEmpty()) {
                PortfolioImage fallbackCover = remaining.get(0);
                fallbackCover.setCover(true);
                portfolioImageRepository.save(fallbackCover);
                log.info("Assigned fallback cover image [{}] for profile [{}]",
                        fallbackCover.getId(), profile.getId());
            } else {
                log.info("Profile [{}] has no remaining portfolio images; no cover set.", profile.getId());
            }
        }
    }


    // ── Queries ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioImage> getPortfolioForUser(Long userId) {
        return photographerProfileRepository.findByUserId(userId)
                .map(profile -> portfolioImageRepository
                        .findByPhotographerProfileIdOrderByDisplayOrderAscCreatedAtDesc(profile.getId()))
                .orElse(List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioImagePublicDto> getPublicPortfolioForProfile(Long profileId) {
        return portfolioImageRepository
                .findByPhotographerProfileIdOrderByDisplayOrderAscCreatedAtDesc(profileId)
                .stream()
                .map(PortfolioImagePublicDto::from)
                .toList();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Validates the uploaded file before any Cloudinary call.
     * Rejects empty files, over-size files, and disallowed content types.
     *
     * WHY content-type and not just file extension:
     *   A user can rename a .exe to .jpg. Content-type from the browser is not
     *   perfect either, but combined with Cloudinary's "resource_type=image"
     *   server-side check it provides reasonable defense-in-depth.
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select a file to upload.");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException(
                    "File size exceeds the 10 MB limit. Please choose a smaller image.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Invalid file type. Only JPEG, PNG, and WEBP images are allowed.");
        }
    }
}
