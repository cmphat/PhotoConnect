package com.photoconnect.service;

import com.photoconnect.dto.UserProfileDto;
import com.photoconnect.dto.UserProfileUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

public interface UserProfileService {

    UserProfileDto getProfile(Long targetUserId, Long currentUserId);

    UserProfileDto getProfileByUsername(String username, Long currentUserId);

    UserProfileDto updateProfile(Long userId, UserProfileUpdateRequest request);

    String updateAvatar(Long userId, MultipartFile file);

    void removeAvatar(Long userId);
}
