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
