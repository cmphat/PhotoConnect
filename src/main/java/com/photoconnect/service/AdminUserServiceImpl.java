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
    private final OperationalAuditService auditService;
    private com.photoconnect.repository.NotificationRepository notificationRepository;

    public AdminUserServiceImpl(UserRepository userRepository) {
        this(userRepository, null, null);
    }

    public AdminUserServiceImpl(UserRepository userRepository, OperationalAuditService auditService) {
        this(userRepository, auditService, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public AdminUserServiceImpl(UserRepository userRepository,
                                @org.springframework.beans.factory.annotation.Autowired(required = false) OperationalAuditService auditService,
                                @org.springframework.beans.factory.annotation.Autowired(required = false) com.photoconnect.repository.NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.notificationRepository = notificationRepository;
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

        requireAdmin(currentAdminId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if (user.getRole() == UserRole.ADMIN && newStatus != UserStatus.ACTIVE
                && userRepository.countByRoleAndStatus(UserRole.ADMIN, UserStatus.ACTIVE) <= 1) {
            throw new IllegalStateException("The last active administrator cannot be suspended.");
        }

        user.setStatus(newStatus);
        User saved = userRepository.save(user);
        audit(currentAdminId, "ACCOUNT_STATUS_CHANGE", userId,
                "Changed account status to " + newStatus.name());
        return saved;
    }

    @Override
    @Transactional
    public User grantManagerRole(Long userId, Long currentAdminId) {
        requireAdmin(currentAdminId);
        if (userId == null || userId.equals(currentAdminId)) {
            throw new IllegalStateException("Administrators cannot promote themselves.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        if (user.getStatus() != UserStatus.ACTIVE || user.getRole() != UserRole.CUSTOMER) {
            throw new IllegalStateException("Only an active CUSTOMER account is eligible for Manager appointment.");
        }
        user.setRole(UserRole.MANAGER);
        User saved = userRepository.save(user);
        if (notificationRepository != null) {
            notificationRepository.deleteAllByRecipientId(userId);
        }
        audit(currentAdminId, "MANAGER_GRANT", userId, "Granted MANAGER role to eligible account");
        return saved;
    }

    @Override
    @Transactional
    public User revokeManagerRole(Long userId, Long currentAdminId) {
        requireAdmin(currentAdminId);
        if (userId == null || userId.equals(currentAdminId)) {
            throw new IllegalStateException("Administrators cannot revoke their own role through Manager operations.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        if (user.getRole() != UserRole.MANAGER) {
            throw new IllegalStateException("The selected account is not a Manager.");
        }
        user.setRole(UserRole.CUSTOMER);
        User saved = userRepository.save(user);
        audit(currentAdminId, "MANAGER_REVOKE", userId, "Revoked MANAGER role; account returned to CUSTOMER");
        return saved;
    }

    private User requireAdmin(Long actorId) {
        if (actorId == null) throw new IllegalStateException("Administrator authentication is required.");
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new IllegalStateException("Administrator account not found."));
        if (actor.getStatus() != UserStatus.ACTIVE || actor.getRole() != UserRole.ADMIN) {
            throw new IllegalStateException("Only an active administrator may manage accounts.");
        }
        return actor;
    }

    private void audit(Long actorId, String action, Long targetId, String summary) {
        if (auditService != null) {
            auditService.record(actorId, action, "User", targetId,
                    com.photoconnect.entity.AuditOutcome.SUCCESS, summary);
        }
    }
}
