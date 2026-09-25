package com.photoconnect.service;

import com.photoconnect.dto.SavedPhotographerDto;

import java.util.List;
import java.util.Set;

public interface SavedPhotographerService {

    /**
     * Saves an approved photographer to the customer's favorites.
     * Idempotent: repeated calls for an already-saved photographer succeed safely.
     *
     * @param customerUserId        Authenticated customer user id.
     * @param photographerProfileId Profile id to save.
     * @return true if saved or already saved.
     * @throws IllegalArgumentException if customer or profile does not exist, or user is not a CUSTOMER.
     * @throws IllegalStateException    if the photographer is not in APPROVED status.
     */
    boolean savePhotographer(Long customerUserId, Long photographerProfileId);

    /**
     * Removes a saved photographer from the customer's favorites.
     * Idempotent: removing an absent favorite returns false gracefully.
     *
     * @param customerUserId        Authenticated customer user id.
     * @param photographerProfileId Profile id to unsave.
     * @return true if removed, false if not previously saved.
     */
    boolean removeSavedPhotographer(Long customerUserId, Long photographerProfileId);

    /**
     * Checks if a photographer profile is saved by the customer.
     * Returns false if customerUserId is null (e.g. guest).
     */
    boolean isSaved(Long customerUserId, Long photographerProfileId);

    /**
     * Returns all saved approved photographers for the customer, ordered by newest saved first.
     * Unapproved photographers are excluded from the active marketplace list while preserving DB rows.
     */
    List<SavedPhotographerDto> getSavedPhotographers(Long customerUserId);

    /**
     * Returns the set of photographer profile IDs saved by the customer.
     */
    Set<Long> getSavedPhotographerProfileIds(Long customerUserId);

    /**
     * Returns the total count of saved approved photographers for the customer.
     */
    long countSavedPhotographers(Long customerUserId);
}
