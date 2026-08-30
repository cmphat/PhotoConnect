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

    public AdminPhotographerServiceImpl(PhotographerProfileRepository photographerProfileRepository) {
        this.photographerProfileRepository = photographerProfileRepository;
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
        // Use findById here — we don't need user fields for mutation
        PhotographerProfile profile = photographerProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Photographer application not found with id: " + profileId));

        if (profile.getVerificationStatus() != PhotographerVerificationStatus.PENDING) {
            throw new InvalidStatusTransitionException(
                    "Cannot approve application with status: " + profile.getVerificationStatus());
        }

        profile.setVerificationStatus(PhotographerVerificationStatus.APPROVED);
        return photographerProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public PhotographerProfile reject(Long profileId) {
        // Use findById here — we don't need user fields for mutation
        PhotographerProfile profile = photographerProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Photographer application not found with id: " + profileId));

        if (profile.getVerificationStatus() != PhotographerVerificationStatus.PENDING) {
            throw new InvalidStatusTransitionException(
                    "Cannot reject application with status: " + profile.getVerificationStatus());
        }

        // NOTE: User.role intentionally remains PHOTOGRAPHER.
        // Account role and professional verification status are separate concepts.
        profile.setVerificationStatus(PhotographerVerificationStatus.REJECTED);
        return photographerProfileRepository.save(profile);
    }
}
