package com.photoconnect.service;

import com.photoconnect.dto.PortfolioImagePublicDto;
import com.photoconnect.entity.PortfolioImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Business logic for photographer portfolio image management.
 *
 * Ownership and verification-status rules are enforced in the implementation.
 * Controllers must NOT bypass this service to access PortfolioImageRepository directly.
 */
public interface PortfolioService {

    /**
     * Uploads a portfolio image for an authenticated, approved photographer.
     *
     * Business rules enforced:
     * 1. The user must have a PhotographerProfile.
     * 2. The profile must have verificationStatus = APPROVED.
     * 3. The file must be a non-empty image (JPEG, PNG, WEBP).
     *
     * @param userId  the authenticated user's id (from session)
     * @param file    the uploaded image file
     * @param caption optional caption
     * @return the saved PortfolioImage entity
     * @throws IllegalStateException    if profile does not exist or is not APPROVED
     * @throws IllegalArgumentException if the file is invalid
     * @throws RuntimeException         if Cloudinary upload fails
     */
    PortfolioImage addPortfolioImage(Long userId, MultipartFile file, String caption);

    /**
     * Deletes a portfolio image, verifying ownership.
     *
     * Business rules enforced:
     * 1. The image must belong to the photographer profile owned by userId.
     * 2. Cloudinary asset is deleted before the DB record.
     * 3. If Cloudinary deletion fails, the DB record is NOT deleted (consistency).
     *
     * @param userId  the authenticated user's id (from session)
     * @param imageId the id of the image to delete
     * @throws IllegalStateException    if no profile exists for this user
     * @throws SecurityException        if the image does not belong to this user's profile
     * @throws IllegalArgumentException if the image does not exist
     * @throws RuntimeException         if Cloudinary deletion fails
     */
    void deletePortfolioImage(Long userId, Long imageId);

    /**
     * Returns all portfolio images for the authenticated user's own profile.
     * Used for the private portfolio management page.
     *
     * @param userId the authenticated user's id (from session)
     * @return list of PortfolioImage entities (or empty list if no profile or no images)
     */
    List<PortfolioImage> getPortfolioForUser(Long userId);

    /**
     * Returns public-safe portfolio image DTOs for a given photographer profile.
     * Used by the public photographer detail page.
     * Does NOT expose publicId.
     *
     * @param profileId the photographer profile's id
     * @return list of public portfolio image DTOs
     */
    List<PortfolioImagePublicDto> getPublicPortfolioForProfile(Long profileId);
}
