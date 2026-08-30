package com.photoconnect.service;

import com.photoconnect.entity.PhotographerProfile;

import java.util.List;

public interface AdminPhotographerService {

    /**
     * Returns all photographer applications currently in PENDING status,
     * ordered oldest-first so admins process them in queue order.
     */
    List<PhotographerProfile> listPendingApplications();

    /**
     * Returns the PhotographerProfile by id for the detail view.
     * Throws IllegalArgumentException if not found.
     */
    PhotographerProfile getApplicationById(Long profileId);

    /**
     * Approves a PENDING application.
     * Only PENDING → APPROVED is allowed.
     * Throws InvalidStatusTransitionException for any other current status.
     */
    PhotographerProfile approve(Long profileId);

    /**
     * Rejects a PENDING application.
     * Only PENDING → REJECTED is allowed.
     * Throws InvalidStatusTransitionException for any other current status.
     * NOTE: User.role remains PHOTOGRAPHER; verification status and account role are separate concepts.
     */
    PhotographerProfile reject(Long profileId);
}
