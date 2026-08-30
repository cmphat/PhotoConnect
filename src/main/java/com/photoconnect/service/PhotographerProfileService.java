package com.photoconnect.service;

import com.photoconnect.dto.PhotographerProfileRequest;
import com.photoconnect.entity.PhotographerProfile;

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
    java.util.Optional<PhotographerProfile> findByUserId(Long userId);
}
