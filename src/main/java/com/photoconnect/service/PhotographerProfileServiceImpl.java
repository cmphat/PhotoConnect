package com.photoconnect.service;

import com.photoconnect.dto.PhotographerProfileEditRequest;
import com.photoconnect.dto.PhotographerProfileRequest;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerSpecialty;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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

        if (user.getRole() != UserRole.CUSTOMER) {
            throw new IllegalStateException("Only customer accounts can apply to become photographers.");
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

    @Override
    @Transactional
    public PhotographerProfile updateProfile(Long userId, PhotographerProfileEditRequest request) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null.");
        }
        if (request == null) {
            throw new IllegalArgumentException("Profile request must not be null.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountDisabledException("Your account is disabled.");
        }

        if (user.getRole() != UserRole.PHOTOGRAPHER) {
            throw new IllegalStateException("Only photographer accounts may edit a photographer profile.");
        }

        PhotographerProfile profile = photographerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Photographer profile not found for user: " + userId));

        // Validate business rules (URLs and specialties)
        String businessRuleError = request.validateBusinessRules();
        if (businessRuleError != null) {
            throw new IllegalArgumentException(businessRuleError);
        }

        // Apply professional updates
        profile.setDisplayName(request.getDisplayName().trim());
        profile.setHeadline(request.getHeadline() != null && !request.getHeadline().trim().isEmpty()
                ? request.getHeadline().trim() : null);
        profile.setBio(request.getBio().trim());
        profile.setCity(request.getCity().trim());
        profile.setCountry(request.getCountry() != null && !request.getCountry().trim().isEmpty()
                ? request.getCountry().trim() : null);
        profile.setExperienceYears(request.getExperienceYears());
        profile.setPriceFrom(request.getPriceFrom());

        // Process controlled specialties
        if (request.getSpecialties() != null && !request.getSpecialties().isEmpty()) {
            List<PhotographerSpecialty> validated = new ArrayList<>();
            for (String s : request.getSpecialties()) {
                if (s != null && !s.trim().isEmpty()) {
                    PhotographerSpecialty spec = PhotographerSpecialty.fromCode(s)
                            .orElseThrow(() -> new IllegalArgumentException("Invalid specialty: " + s));
                    validated.add(spec);
                }
            }
            profile.setSpecialtiesList(validated);
        } else {
            profile.setSpecialties(null);
        }

        // URLs with safe normalization
        profile.setWebsiteUrl(request.getWebsiteUrl() != null && !request.getWebsiteUrl().trim().isEmpty()
                ? request.getWebsiteUrl().trim() : null);
        profile.setInstagramUrl(request.getInstagramUrl() != null && !request.getInstagramUrl().trim().isEmpty()
                ? request.getInstagramUrl().trim() : null);
        profile.setFacebookUrl(request.getFacebookUrl() != null && !request.getFacebookUrl().trim().isEmpty()
                ? request.getFacebookUrl().trim() : null);

        // Optional creator details
        profile.setEquipmentSummary(request.getEquipmentSummary() != null && !request.getEquipmentSummary().trim().isEmpty()
                ? request.getEquipmentSummary().trim() : null);
        profile.setLanguages(request.getLanguages() != null && !request.getLanguages().trim().isEmpty()
                ? request.getLanguages().trim() : null);
        profile.setTravelAvailable(request.getTravelAvailable());

        // CRITICAL INVARIANT: verificationStatus, averageRating, reviewCount, and user.role
        // are NEVER mutated by profile edit operations.

        return photographerProfileRepository.save(profile);
    }

    @Override
    public int calculateProfileCompleteness(PhotographerProfile profile, int portfolioImageCount, boolean hasCoverImage) {
        if (profile == null) {
            return 0;
        }
        int score = 0;
        // 1. Professional Name (10%)
        if (profile.getDisplayName() != null && !profile.getDisplayName().trim().isEmpty()) {
            score += 10;
        }
        // 2. Headline (15%)
        if (profile.getHeadline() != null && !profile.getHeadline().trim().isEmpty()) {
            score += 15;
        }
        // 3. Biography (15%)
        if (profile.getBio() != null && profile.getBio().trim().length() >= 20) {
            score += 15;
        }
        // 4. Location (10%)
        if ((profile.getCity() != null && !profile.getCity().trim().isEmpty())
                || (profile.getCountry() != null && !profile.getCountry().trim().isEmpty())) {
            score += 10;
        }
        // 5. Specialties (15%)
        if (profile.getSpecialties() != null && !profile.getSpecialties().trim().isEmpty()) {
            score += 15;
        }
        // 6. Starting Price (10%)
        if (profile.getPriceFrom() != null && profile.getPriceFrom().compareTo(BigDecimal.ZERO) > 0) {
            score += 10;
        }
        // 7. Portfolio Images (15%)
        if (portfolioImageCount >= 1) {
            score += 15;
        }
        // 8. Dedicated Cover Image (10%)
        if (hasCoverImage) {
            score += 10;
        }
        return Math.min(score, 100);
    }

    @Override
    public List<String> getCompletenessRecommendations(PhotographerProfile profile, int portfolioImageCount, boolean hasCoverImage) {
        List<String> recommendations = new ArrayList<>();
        if (profile == null) {
            recommendations.add("Complete your basic creator profile.");
            return recommendations;
        }
        if (profile.getHeadline() == null || profile.getHeadline().trim().isEmpty()) {
            recommendations.add("Add a professional headline describing your photography style.");
        }
        if (profile.getSpecialties() == null || profile.getSpecialties().trim().isEmpty()) {
            recommendations.add("Select at least one photography specialty to help clients find your work.");
        }
        if (profile.getCountry() == null || profile.getCountry().trim().isEmpty()) {
            recommendations.add("Specify your country or territory for regional discovery.");
        }
        if (profile.getPriceFrom() == null || profile.getPriceFrom().compareTo(BigDecimal.ZERO) <= 0) {
            recommendations.add("Set a realistic starting price (VND) for prospective clients.");
        }
        if (portfolioImageCount == 0) {
            recommendations.add("Upload photographs to build your portfolio.");
        } else if (!hasCoverImage) {
            recommendations.add("Designate a portfolio photograph as your primary cover.");
        }
        if (profile.getWebsiteUrl() == null && profile.getInstagramUrl() == null && profile.getFacebookUrl() == null) {
            recommendations.add("Add links to your social channels or website.");
        }
        return recommendations;
    }
}
