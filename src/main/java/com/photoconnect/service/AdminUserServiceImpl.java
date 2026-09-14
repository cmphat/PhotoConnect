package com.photoconnect.service;

import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;

    public AdminUserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> searchUsers(String search, UserRole role, UserStatus status) {
        String normalizedSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        return userRepository.searchUsers(normalizedSearch, role, status);
    }

    @Override
    @Transactional
    public User updateUserStatus(Long userId, UserStatus newStatus, Long currentAdminId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null.");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("Target status must not be null.");
        }
        if (userId.equals(currentAdminId)) {
            throw new IllegalStateException("Administrators cannot modify their own account status.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        user.setStatus(newStatus);
        return userRepository.save(user);
    }
}
