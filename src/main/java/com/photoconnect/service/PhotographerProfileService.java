package com.photoconnect.service;

import com.photoconnect.dto.PhotographerProfileEditRequest;
import com.photoconnect.dto.PhotographerProfileRequest;
import com.photoconnect.entity.PhotographerProfile;

import java.util.List;
import java.util.Optional;

public interface PhotographerProfileService {

    /**
     * Creates a PhotographerProfile for the authenticated user.
     * Also promotes the user's role from CUSTOMER to PHOTOGRAPHER.
     * Both operations are atomic.
     *
     * @param userId  ID of the currently logged-in user (from session)
     * @param request validated form data from the onboarding form
     * @return the persisted PhotographerProfile
     */
    PhotographerProfile createProfile(Long userId, PhotographerProfileRequest request);

    /**
     * Returns the PhotographerProfile associated with the given userId, if any.
     */
    Optional<PhotographerProfile> findByUserId(Long userId);

    /**
     * Updates professional profile information for the authenticated photographer.
     * Strictly verifies ownership, validates specialties against controlled taxonomy,
     * and validates URL schemes.
     * Preserves verificationStatus, ratings, review count, and account role.
     *
     * @param userId  ID of the currently logged-in user (from session)
     * @param request validated form data from the profile edit form
     * @return the persisted PhotographerProfile
     */
    PhotographerProfile updateProfile(Long userId, PhotographerProfileEditRequest request);

    /**
     * Calculates the profile completeness percentage (0 to 100) based on actual profile state.
     *
     * @param profile             the photographer profile entity
     * @param portfolioImageCount number of portfolio images uploaded
     * @param hasCoverImage       whether a dedicated cover image is designated
     * @return completeness percentage from 0 to 100
     */
    int calculateProfileCompleteness(PhotographerProfile profile, int portfolioImageCount, boolean hasCoverImage);

    /**
     * Returns actionable, human-friendly recommendations for completing the profile.
     */
    List<String> getCompletenessRecommendations(PhotographerProfile profile, int portfolioImageCount, boolean hasCoverImage);
}
