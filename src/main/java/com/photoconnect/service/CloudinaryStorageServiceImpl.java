package com.photoconnect.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Cloudinary SDK implementation of CloudinaryStorageService.
 *
 * WHY this class is kept thin:
 *   Business logic (who can upload, ownership checks) belongs in PortfolioService.
 *   This class does only one thing: talk to the Cloudinary API. Keeping it thin
 *   makes it easy to replace with a different provider later.
 *
 * WHY we use ObjectUtils.asMap() instead of HashMap:
 *   This is the idiomatic Cloudinary SDK approach. ObjectUtils.asMap() accepts
 *   varargs key-value pairs and returns a Map<String, Object> for the upload params.
 */
@Service
public class CloudinaryStorageServiceImpl implements CloudinaryStorageService {

    private final Cloudinary cloudinary;

    public CloudinaryStorageServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Uploads a MultipartFile to Cloudinary.
     *
     * "folder" controls the Cloudinary folder path so assets are organized:
     *   photoconnect/portfolio/{generated-public-id}
     *
     * "resource_type" = "image" — rejects non-image content at the Cloudinary level.
     *
     * WHY we pass file.getBytes() and not an InputStream:
     *   The Cloudinary SDK's upload() method accepts byte[] directly; this avoids
     *   the complication of stream-position management with MultipartFile streams.
     *
     * @throws RuntimeException wrapping IOException if Cloudinary API call fails.
     */
    @Override
    public CloudinaryUploadResult uploadImage(MultipartFile file, String folder) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "image"
                    )
            );
            String secureUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");
            return new CloudinaryUploadResult(secureUrl, publicId);
        } catch (IOException e) {
            throw new RuntimeException("Cloudinary upload failed: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes an asset from Cloudinary by publicId.
     *
     * WHY we throw on failure:
     *   The caller (PortfolioService) needs to know if Cloudinary deletion failed
     *   so it can handle partial failures (e.g., avoid deleting the DB record if
     *   the asset still exists in Cloudinary). Swallowing the exception here would
     *   make the service layer unable to react.
     */
    @Override
    public void deleteImage(String publicId) {
        try {
            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap("resource_type", "image")
            );
        } catch (Exception e) {
            throw new RuntimeException("Cloudinary deletion failed for publicId [" + publicId + "]: " + e.getMessage(), e);
        }
    }
}
