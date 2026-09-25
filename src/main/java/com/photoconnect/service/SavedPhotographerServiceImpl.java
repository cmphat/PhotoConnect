package com.photoconnect.service;

import com.photoconnect.dto.PhotographerPublicDto;
import com.photoconnect.dto.SavedPhotographerDto;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.SavedPhotographer;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.repository.SavedPhotographerRepository;
import com.photoconnect.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class SavedPhotographerServiceImpl implements SavedPhotographerService {

    private static final Logger log = LoggerFactory.getLogger(SavedPhotographerServiceImpl.class);

    private final SavedPhotographerRepository savedPhotographerRepository;
    private final UserRepository userRepository;
    private final PhotographerProfileRepository photographerProfileRepository;
    private final PublicPhotographerService publicPhotographerService;

    public SavedPhotographerServiceImpl(SavedPhotographerRepository savedPhotographerRepository,
                                        UserRepository userRepository,
                                        PhotographerProfileRepository photographerProfileRepository,
                                        PublicPhotographerService publicPhotographerService) {
        this.savedPhotographerRepository = savedPhotographerRepository;
        this.userRepository = userRepository;
        this.photographerProfileRepository = photographerProfileRepository;
        this.publicPhotographerService = publicPhotographerService;
    }

    @Override
    @Transactional
    public boolean savePhotographer(Long customerUserId, Long photographerProfileId) {
        if (customerUserId == null) {
            throw new IllegalArgumentException("Customer user ID is required.");
        }
        if (photographerProfileId == null) {
            throw new IllegalArgumentException("Photographer profile ID is required.");
        }

        User customer = userRepository.findById(customerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + customerUserId));

        if (customer.getRole() != UserRole.CUSTOMER) {
            throw new IllegalArgumentException("Only users with CUSTOMER role can save photographers.");
        }

        PhotographerProfile profile = photographerProfileRepository.findById(photographerProfileId)
                .orElseThrow(() -> new IllegalArgumentException("Photographer profile not found with id: " + photographerProfileId));

        if (profile.getVerificationStatus() != PhotographerVerificationStatus.APPROVED) {
            throw new IllegalStateException("Only approved photographers can be saved to favorites.");
        }

        // Idempotency check: if already saved, return true
        if (savedPhotographerRepository.existsByCustomerIdAndPhotographerProfileId(customerUserId, photographerProfileId)) {
            log.debug("Photographer {} is already saved by customer {}", photographerProfileId, customerUserId);
            return true;
        }

        try {
            SavedPhotographer saved = new SavedPhotographer(customer, profile);
            savedPhotographerRepository.save(saved);
            log.info("Saved photographer {} for customer {}", photographerProfileId, customerUserId);
            return true;
        } catch (DataIntegrityViolationException e) {
            // Concurrent double-post caught by unique constraint
            log.warn("Concurrent duplicate save detected for customer {} and profile {}: {}",
                    customerUserId, photographerProfileId, e.getMessage());
            return true;
        }
    }

    @Override
    @Transactional
    public boolean removeSavedPhotographer(Long customerUserId, Long photographerProfileId) {
        if (customerUserId == null || photographerProfileId == null) {
            return false;
        }

        Optional<SavedPhotographer> existing = savedPhotographerRepository
                .findByCustomerIdAndPhotographerProfileId(customerUserId, photographerProfileId);

        if (existing.isPresent()) {
            savedPhotographerRepository.delete(existing.get());
            log.info("Removed saved photographer {} for customer {}", photographerProfileId, customerUserId);
            return true;
        }

        log.debug("Photographer {} was not saved by customer {}, remove is no-op", photographerProfileId, customerUserId);
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSaved(Long customerUserId, Long photographerProfileId) {
        if (customerUserId == null || photographerProfileId == null) {
            return false;
        }
        return savedPhotographerRepository.existsByCustomerIdAndPhotographerProfileId(customerUserId, photographerProfileId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavedPhotographerDto> getSavedPhotographers(Long customerUserId) {
        if (customerUserId == null) {
            return Collections.emptyList();
        }

        List<SavedPhotographer> list = savedPhotographerRepository.findApprovedByCustomerIdWithProfile(customerUserId);
        List<SavedPhotographerDto> result = new ArrayList<>(list.size());

        for (SavedPhotographer sp : list) {
            try {
                PhotographerPublicDto publicDto = publicPhotographerService
                        .getApprovedPhotographerById(sp.getPhotographerProfile().getId());
                result.add(SavedPhotographerDto.from(sp, publicDto));
            } catch (IllegalArgumentException e) {
                // In case status changed between query and DTO resolution
                log.debug("Skipping non-approved saved photographer {}: {}",
                        sp.getPhotographerProfile().getId(), e.getMessage());
            }
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getSavedPhotographerProfileIds(Long customerUserId) {
        if (customerUserId == null) {
            return Collections.emptySet();
        }
        return new HashSet<>(savedPhotographerRepository.findSavedPhotographerProfileIdsByCustomerId(customerUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public long countSavedPhotographers(Long customerUserId) {
        if (customerUserId == null) {
            return 0;
        }
        return savedPhotographerRepository.findApprovedByCustomerIdWithProfile(customerUserId).size();
    }
}
