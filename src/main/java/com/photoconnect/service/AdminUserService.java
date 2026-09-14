package com.photoconnect.service;

import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;

import java.util.List;

public interface AdminUserService {

    /**
     * Searches and filters users by keyword, role, and status.
     */
    List<User> searchUsers(String search, UserRole role, UserStatus status);

    /**
     * Updates the status of a user (ACTIVE, INACTIVE, BANNED).
     * Prevents an administrator from altering their own account status.
     */
    User updateUserStatus(Long userId, UserStatus newStatus, Long currentAdminId);
}
