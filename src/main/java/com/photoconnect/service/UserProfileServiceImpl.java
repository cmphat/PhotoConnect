package com.photoconnect.service;

import com.photoconnect.dto.UserProfileDto;
import com.photoconnect.dto.UserProfileUpdateRequest;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PortfolioImage;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.repository.ReviewRepository;
import com.photoconnect.repository.UserRepository;
import com.photoconnect.util.ImageValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    private static final Logger log = LoggerFactory.getLogger(UserProfileServiceImpl.class);
    private static final String AVATAR_FOLDER = "photoconnect/avatars";
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/webp"
    );
    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024; // 5MB

    private final UserRepository userRepository;
    private final PhotographerProfileRepository photographerProfileRepository;
    private final PortfolioService portfolioService;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final CloudinaryStorageService cloudinaryStorageService;

    public UserProfileServiceImpl(UserRepository userRepository,
                                  PhotographerProfileRepository photographerProfileRepository,
                                  PortfolioService portfolioService,
                                  BookingRepository bookingRepository,
                                  ReviewRepository reviewRepository,
                                  CloudinaryStorageService cloudinaryStorageService) {
        this.userRepository = userRepository;
        this.photographerProfileRepository = photographerProfileRepository;
        this.portfolioService = portfolioService;
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
        this.cloudinaryStorageService = cloudinaryStorageService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getProfile(Long targetUserId, Long currentUserId) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + targetUserId));
        return buildProfileDto(user, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getProfileByUsername(String username, Long currentUserId) {
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> {
                    // Fallback: try finding by email prefix if username is not explicitly set
                    List<User> all = userRepository.findAll();
                    return all.stream()
                            .filter(u -> username.equalsIgnoreCase(u.getUsernameOrHandle()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
                });
        return buildProfileDto(user, currentUserId);
    }

    private UserProfileDto buildProfileDto(User user, Long currentUserId) {
        UserProfileDto dto = new UserProfileDto();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsernameOrHandle());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole().name());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setBio(user.getBio());
        dto.setLocation(user.getLocation());
        dto.setWebsiteUrl(user.getWebsiteUrl());
        dto.setFacebookUrl(user.getFacebookUrl());
        dto.setInstagramUrl(user.getInstagramUrl());
        dto.setTiktokUrl(user.getTiktokUrl());
        dto.setYoutubeUrl(user.getYoutubeUrl());

        boolean isOwner = currentUserId != null && currentUserId.equals(user.getId());
        dto.setOwner(isOwner);

        // Sensitive information only visible to owner
        if (isOwner) {
            dto.setEmail(user.getEmail());
            dto.setPhone(user.getPhone());
        }

        // Check if PhotographerProfile exists
        if (user.getRole() == UserRole.PHOTOGRAPHER) {
            Optional<PhotographerProfile> profileOpt = photographerProfileRepository.findByUserId(user.getId());
            if (profileOpt.isPresent()) {
                PhotographerProfile profile = profileOpt.get();
                dto.setPhotographerProfileId(profile.getId());
                if (dto.getDisplayName() == null || dto.getDisplayName().isBlank()) {
                    dto.setDisplayName(profile.getDisplayName());
                }
                if (dto.getBio() == null || dto.getBio().isBlank()) {
                    dto.setBio(profile.getBio());
                }
                if (dto.getLocation() == null || dto.getLocation().isBlank()) {
                    dto.setLocation(profile.getCity());
                }
                if (dto.getWebsiteUrl() == null || dto.getWebsiteUrl().isBlank()) {
                    dto.setWebsiteUrl(profile.getWebsiteUrl());
                }
                if (dto.getFacebookUrl() == null || dto.getFacebookUrl().isBlank()) {
                    dto.setFacebookUrl(profile.getFacebookUrl());
                }
                if (dto.getInstagramUrl() == null || dto.getInstagramUrl().isBlank()) {
                    dto.setInstagramUrl(profile.getInstagramUrl());
                }

                dto.setAverageRating(profile.getAverageRating());
                dto.setReviewsCount(profile.getReviewCount());

                // Fetch portfolio images
                try {
                    List<PortfolioImage> images = portfolioService.getPortfolioForUser(user.getId());
                    dto.setPortfolioImages(images);
                } catch (Exception e) {
                    log.warn("Could not fetch portfolio images for user: {}", user.getId(), e);
                }
            }
        }

        // Set bookings count if available
        try {
            if (user.getRole() == UserRole.PHOTOGRAPHER) {
                Optional<PhotographerProfile> profileOpt = photographerProfileRepository.findByUserId(user.getId());
                profileOpt.ifPresent(p -> {
                    long count = bookingRepository.countByPhotographerProfileId(p.getId());
                    dto.setTotalBookings((int) count);
                });
            } else {
                long count = bookingRepository.countByCustomerId(user.getId());
                dto.setTotalBookings((int) count);
            }
        } catch (Exception ignored) {
        }

        return dto;
    }

    @Override
    @Transactional
    public UserProfileDto updateProfile(Long userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if (request.getDisplayName() != null && !request.getDisplayName().isBlank()) {
            user.setFullName(request.getDisplayName().trim());
        }
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            String newUsername = request.getUsername().trim().toLowerCase();
            Optional<User> existing = userRepository.findByUsername(newUsername);
            if (existing.isPresent() && !existing.get().getId().equals(userId)) {
                throw new IllegalArgumentException("Username is already taken by another user.");
            }
            user.setUsername(newUsername);
        }

        user.setBio(request.getBio() != null ? request.getBio().trim() : null);
        user.setLocation(request.getLocation() != null ? request.getLocation().trim() : null);
        user.setWebsiteUrl(sanitizeUrl(request.getWebsiteUrl()));
        user.setFacebookUrl(sanitizeUrl(request.getFacebookUrl()));
        user.setInstagramUrl(sanitizeUrl(request.getInstagramUrl()));
        user.setTiktokUrl(sanitizeUrl(request.getTiktokUrl()));
        user.setYoutubeUrl(sanitizeUrl(request.getYoutubeUrl()));

        userRepository.save(user);

        // Sync with PhotographerProfile if photographer
        if (user.getRole() == UserRole.PHOTOGRAPHER) {
            Optional<PhotographerProfile> profileOpt = photographerProfileRepository.findByUserId(userId);
            if (profileOpt.isPresent()) {
                PhotographerProfile profile = profileOpt.get();
                if (request.getDisplayName() != null && !request.getDisplayName().isBlank()) {
                    profile.setDisplayName(request.getDisplayName().trim());
                }
                if (request.getBio() != null) {
                    profile.setBio(request.getBio());
                }
                if (request.getLocation() != null) {
                    profile.setCity(request.getLocation());
                }
                if (request.getWebsiteUrl() != null) {
                    profile.setWebsiteUrl(request.getWebsiteUrl());
                }
                if (request.getFacebookUrl() != null) {
                    profile.setFacebookUrl(request.getFacebookUrl());
                }
                if (request.getInstagramUrl() != null) {
                    profile.setInstagramUrl(request.getInstagramUrl());
                }
                photographerProfileRepository.save(profile);
            }
        }

        return buildProfileDto(user, userId);
    }

    @Override
    @Transactional
    public String updateAvatar(Long userId, MultipartFile file) {
        ImageValidationUtils.validateImage(file, MAX_AVATAR_SIZE);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // Delete previous avatar from Cloudinary if exists
        String oldPublicId = user.getAvatarPublicId();
        if (oldPublicId != null && !oldPublicId.isBlank()) {
            try {
                cloudinaryStorageService.deleteImage(oldPublicId);
            } catch (Exception e) {
                log.warn("Failed to delete old avatar [{}] from Cloudinary: {}", oldPublicId, e.getMessage());
            }
        }

        // Upload new avatar to Cloudinary
        CloudinaryStorageService.CloudinaryUploadResult result =
                cloudinaryStorageService.uploadImage(file, AVATAR_FOLDER);

        user.setAvatarUrl(result.secureUrl());
        user.setAvatarPublicId(result.publicId());
        userRepository.save(user);

        log.info("Updated avatar for user [{}], secureUrl: [{}]", userId, result.secureUrl());
        return result.secureUrl();
    }

    @Override
    @Transactional
    public void removeAvatar(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        String oldPublicId = user.getAvatarPublicId();
        if (oldPublicId != null && !oldPublicId.isBlank()) {
            try {
                cloudinaryStorageService.deleteImage(oldPublicId);
            } catch (Exception e) {
                log.warn("Failed to delete avatar [{}] from Cloudinary: {}", oldPublicId, e.getMessage());
            }
        }

        user.setAvatarUrl(null);
        user.setAvatarPublicId(null);
        userRepository.save(user);
        log.info("Removed avatar for user [{}]", userId);
    }

    private String sanitizeUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String trimmed = url.trim();
        String lower = trimmed.toLowerCase();
        if (lower.startsWith("javascript:") || lower.startsWith("data:") || lower.startsWith("vbscript:")) {
            throw new IllegalArgumentException("Giao thức URL không an toàn.");
        }
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            if (!trimmed.contains("://")) {
                trimmed = "https://" + trimmed;
            } else {
                throw new IllegalArgumentException("Đường dẫn liên kết phải sử dụng giao thức HTTP hoặc HTTPS hợp lệ.");
            }
        }
        return trimmed;
    }
}
