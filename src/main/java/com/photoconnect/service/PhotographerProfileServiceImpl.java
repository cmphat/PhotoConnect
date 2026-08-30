package com.photoconnect.service;

import com.photoconnect.dto.PhotographerProfileRequest;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.exception.AccountDisabledException;
import com.photoconnect.exception.PhotographerProfileAlreadyExistsException;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PhotographerProfileServiceImpl implements PhotographerProfileService {

    private final PhotographerProfileRepository photographerProfileRepository;
    private final UserRepository userRepository;

    public PhotographerProfileServiceImpl(PhotographerProfileRepository photographerProfileRepository,
                                          UserRepository userRepository) {
        this.photographerProfileRepository = photographerProfileRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public PhotographerProfile createProfile(Long userId, PhotographerProfileRequest request) {
        // 1. Load the authenticated user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // 2. Reject inactive/banned users
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountDisabledException("Your account must be active to apply as a photographer.");
        }

        // 3. Prevent duplicate profiles (service-level check before DB constraint)
        if (photographerProfileRepository.existsByUserId(userId)) {
            throw new PhotographerProfileAlreadyExistsException("A photographer profile already exists for this account.");
        }

        // 4. Build and persist the profile
        PhotographerProfile profile = new PhotographerProfile();
        profile.setUser(user);
        profile.setDisplayName(request.getDisplayName().trim());
        profile.setBio(request.getBio().trim());
        profile.setCity(request.getCity().trim());
        profile.setExperienceYears(request.getExperienceYears());
        profile.setPriceFrom(request.getPriceFrom());
        profile.setVerificationStatus(PhotographerVerificationStatus.PENDING);

        // 5. Promote the user's role to PHOTOGRAPHER
        //    Note: This does NOT mean the profile is publicly approved.
        //    verificationStatus=PENDING is a separate concept from the account role.
        user.setRole(UserRole.PHOTOGRAPHER);
        userRepository.save(user);

        return photographerProfileRepository.save(profile);
    }

    @Override
    public Optional<PhotographerProfile> findByUserId(Long userId) {
        return photographerProfileRepository.findByUserId(userId);
    }
}
