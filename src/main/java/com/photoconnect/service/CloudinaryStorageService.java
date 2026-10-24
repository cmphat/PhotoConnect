package com.photoconnect.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraction for Cloudinary binary storage operations.
 *
 * WHY an interface:
 *   - Business services (PortfolioService) depend on this interface, not on the
 *     Cloudinary SDK directly. This makes PortfolioService unit-testable without
 *     a real Cloudinary account — tests mock this interface.
 *   - If storage provider changes in the future, only the implementation changes.
 */
public interface CloudinaryStorageService {

    /**
     * Uploads an image file to Cloudinary.
     *
     * @param file   the multipart image file from the upload form
     * @param folder the Cloudinary folder path (e.g. "photoconnect/portfolio")
     * @return an upload result containing the secure HTTPS URL and publicId
     * @throws RuntimeException if the upload fails
     */
    CloudinaryUploadResult uploadImage(MultipartFile file, String folder);

    /**
     * Uploads a private/authenticated image file to Cloudinary for customer deliverables.
     * Assets stored under authenticated delivery cannot be accessed anonymously without a valid signature.
     *
     * @param file   the multipart image file
     * @param folder the Cloudinary folder path
     * @return an upload result containing the authenticated secure URL and publicId
     */
    CloudinaryUploadResult uploadPrivateImage(MultipartFile file, String folder);

    /**
     * Generates a time-limited, cryptographically signed Cloudinary delivery URL.
     *
     * @param publicId          the Cloudinary public identifier
     * @param deliveryType      delivery type ("authenticated" or "upload")
     * @param transformation    optional transformation string (e.g. "fl_attachment", "c_fill,w_500,h_500")
     * @param expirationSeconds validity lifetime in seconds (e.g. 300)
     * @return signed, time-limited URL string
     */
    String generateAuthorizedUrl(String publicId, String deliveryType, String transformation, long expirationSeconds);

    /**
     * Safely migrates an existing public "upload" asset to "authenticated" mode.
     *
     * @param publicId the asset public identifier
     * @return true if migration succeeded in Cloudinary, false otherwise
     */
    boolean migrateAssetToAuthenticated(String publicId);

    /**
     * Deletes an asset from Cloudinary by its publicId.
     *
     * @param publicId the Cloudinary asset public ID (stored in portfolio_images.public_id)
     * @throws RuntimeException if the deletion fails
     */
    void deleteImage(String publicId);

    /**
     * Result record from a successful Cloudinary upload.
     *
     * @param secureUrl the HTTPS URL to render the image (stored in image_url column)
     * @param publicId  the Cloudinary asset identifier (stored in public_id column)
     */
    record CloudinaryUploadResult(String secureUrl, String publicId) {
    }
}
