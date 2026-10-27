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

    PhotographerProfile approve(Long profileId, Long adminId, String adminNotes);

    PhotographerProfile reject(Long profileId);

    PhotographerProfile reject(Long profileId, Long adminId, String rejectionReason, String adminNotes);

    PhotographerProfile suspend(Long profileId, Long adminId, String suspensionReason, String adminNotes);

    /**
     * Returns all photographer profiles with associated User fetched.
     */
    List<PhotographerProfile> listAllPhotographers();

    /**
     * Returns photographer profiles filtered by verification status with associated User fetched.
     */
    List<PhotographerProfile> listPhotographersByStatus(com.photoconnect.entity.PhotographerVerificationStatus status);
}
