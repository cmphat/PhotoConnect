package com.photoconnect.service;

import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.exception.InvalidStatusTransitionException;
import com.photoconnect.repository.PhotographerProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminPhotographerServiceImpl implements AdminPhotographerService {

    private final PhotographerProfileRepository photographerProfileRepository;
    private final OperationalAuditService auditService;

    public AdminPhotographerServiceImpl(PhotographerProfileRepository photographerProfileRepository) {
        this(photographerProfileRepository, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public AdminPhotographerServiceImpl(PhotographerProfileRepository photographerProfileRepository,
                                        @org.springframework.beans.factory.annotation.Autowired(required = false) OperationalAuditService auditService) {
        this.photographerProfileRepository = photographerProfileRepository;
        this.auditService = auditService;
    }

    /**
     * Returns PENDING applications with the associated User fully initialized
     * via JPQL JOIN FETCH, inside a read-only transaction.
     *
     * WHY JOIN FETCH and not EAGER or open-in-view=true:
     *   - open-in-view=true keeps the Hibernate session open for the whole HTTP
     *     request, hiding N+1 issues and making session lifetime unpredictable.
     *     This project explicitly disables it (open-in-view=false).
     *   - Changing FetchType.EAGER globally forces the User to be loaded on
     *     every PhotographerProfile query, including places that don't need it.
     *   - JOIN FETCH loads exactly the data this query needs in one SQL JOIN,
     *     inside the transaction boundary, so the User is already initialized
     *     before the JSP receives it.
     */
    @Override
    @Transactional(readOnly = true)
    public List<PhotographerProfile> listPendingApplications() {
        return photographerProfileRepository
                .findByVerificationStatusWithUser(PhotographerVerificationStatus.PENDING);
    }

    /**
     * Returns a single profile with the associated User fully initialized.
     * The JOIN FETCH query prevents LazyInitializationException in the JSP.
     */
    @Override
    @Transactional(readOnly = true)
    public PhotographerProfile getApplicationById(Long profileId) {
        return photographerProfileRepository.findByIdWithUser(profileId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Photographer application not found with id: " + profileId));
    }

    @Override
    @Transactional
    public PhotographerProfile approve(Long profileId) {
        return approve(profileId, null, null);
    }

    @Override
    @Transactional
    public PhotographerProfile approve(Long profileId, Long adminId, String adminNotes) {
        PhotographerProfile profile = photographerProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Photographer application not found with id: " + profileId));

        if (profile.getVerificationStatus() != PhotographerVerificationStatus.PENDING) {
            throw new InvalidStatusTransitionException(
                    "Cannot approve application with status: " + profile.getVerificationStatus());
        }

        profile.setVerificationStatus(PhotographerVerificationStatus.APPROVED);
        profile.setReviewedAt(java.time.LocalDateTime.now());
        profile.setReviewedByAdminId(adminId);
        if (adminNotes != null && !adminNotes.isBlank()) {
            profile.setAdminNotes(adminNotes.trim());
        }
        PhotographerProfile saved = photographerProfileRepository.save(profile);
        if (auditService != null && adminId != null) {
            auditService.record(adminId, "PHOTOGRAPHER_APPROVE", "PhotographerProfile", profileId,
                    com.photoconnect.entity.AuditOutcome.SUCCESS, "Approved eligible photographer application");
        }
        return saved;
    }

    @Override
    @Transactional
    public PhotographerProfile reject(Long profileId) {
        return reject(profileId, null, null, null);
    }

    @Override
    @Transactional
    public PhotographerProfile reject(Long profileId, Long adminId, String rejectionReason, String adminNotes) {
        PhotographerProfile profile = photographerProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Photographer application not found with id: " + profileId));

        if (profile.getVerificationStatus() != PhotographerVerificationStatus.PENDING) {
            throw new InvalidStatusTransitionException(
                    "Cannot reject application with status: " + profile.getVerificationStatus());
        }

        profile.setVerificationStatus(PhotographerVerificationStatus.REJECTED);
        profile.setReviewedAt(java.time.LocalDateTime.now());
        profile.setReviewedByAdminId(adminId);
        if (rejectionReason != null && !rejectionReason.isBlank()) {
            profile.setRejectionReason(rejectionReason.trim());
        }
        if (adminNotes != null && !adminNotes.isBlank()) {
            profile.setAdminNotes(adminNotes.trim());
        }
        PhotographerProfile saved = photographerProfileRepository.save(profile);
        if (auditService != null && adminId != null) {
            auditService.record(adminId, "PHOTOGRAPHER_REJECT", "PhotographerProfile", profileId,
                    com.photoconnect.entity.AuditOutcome.SUCCESS, "Rejected photographer application: " + rejectionReason);
        }
        return saved;
    }

    @Override
    @Transactional
    public PhotographerProfile suspend(Long profileId, Long adminId, String suspensionReason, String adminNotes) {
        PhotographerProfile profile = photographerProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Photographer application not found with id: " + profileId));

        if (profile.getVerificationStatus() != PhotographerVerificationStatus.APPROVED) {
            throw new InvalidStatusTransitionException(
                    "Cannot suspend photographer with status: " + profile.getVerificationStatus() + ". Only APPROVED photographers can be suspended.");
        }

        profile.setVerificationStatus(PhotographerVerificationStatus.SUSPENDED);
        profile.setReviewedAt(java.time.LocalDateTime.now());
        profile.setReviewedByAdminId(adminId);
        if (suspensionReason != null && !suspensionReason.isBlank()) {
            profile.setRejectionReason(suspensionReason.trim());
        }
        if (adminNotes != null && !adminNotes.isBlank()) {
            profile.setAdminNotes(adminNotes.trim());
        }
        PhotographerProfile saved = photographerProfileRepository.save(profile);
        if (auditService != null && adminId != null) {
            auditService.record(adminId, "PHOTOGRAPHER_SUSPEND", "PhotographerProfile", profileId,
                    com.photoconnect.entity.AuditOutcome.SUCCESS, "Suspended photographer profile: " + suspensionReason);
        }
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhotographerProfile> listAllPhotographers() {
        return photographerProfileRepository.findAllWithUser();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhotographerProfile> listPhotographersByStatus(PhotographerVerificationStatus status) {
        if (status == null) {
            return photographerProfileRepository.findAllWithUser();
        }
        return photographerProfileRepository.findByVerificationStatusWithUser(status);
    }
}
