package com.photoconnect.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring configuration for the Cloudinary SDK client.
 *
 * WHY environment variables, not application.properties values:
 *   application.properties is committed to Git. Any value written directly into
 *   application.properties becomes part of the repository history and could leak.
 *   Using ${CLOUDINARY_CLOUD_NAME:} in application.properties means:
 *     - At runtime, Spring reads the OS environment variable
 *     - The default (after the colon) is empty string — no value committed
 *
 * WHY we don't fail on startup if credentials are empty:
 *   For local development without Cloudinary, the bean is still created — it just
 *   won't work when an actual upload is attempted. The failure surfaces at call time
 *   (upload endpoint), not at startup, which is friendlier for developers running
 *   only the database/auth parts of the application.
 *
 *   If strict startup validation is needed in production, add a @PostConstruct check.
 */
@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    /**
     * Produces the Cloudinary SDK client bean.
     *
     * The Cloudinary constructor accepts a Map of configuration.
     * We use "secure=true" to always generate HTTPS URLs.
     */
    @Bean
    public Cloudinary cloudinary() {
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        config.put("secure", true);
        return new Cloudinary(config);
    }
}
