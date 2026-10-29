package com.photoconnect.security;

import com.photoconnect.entity.UserRole;

import java.util.EnumSet;
import java.util.Set;

public final class RolePermissionPolicy {

    private static final Set<PlatformPermission> MANAGER_PERMISSIONS = EnumSet.of(
            PlatformPermission.PHOTOGRAPHER_APPLICATION_REVIEW,
            PlatformPermission.USER_REPORT_REVIEW,
            PlatformPermission.BOOKING_OPERATIONAL_VIEW,
            PlatformPermission.BOOKING_DISPUTE_RESOLVE,
            PlatformPermission.SUPPORT_TICKET_MANAGE,
            PlatformPermission.OPERATIONAL_ANALYTICS_VIEW
    );

    private RolePermissionPolicy() {
    }

    public static boolean hasPermission(UserRole role, PlatformPermission permission) {
        if (role == null || permission == null) {
            return false;
        }
        if (role == UserRole.ADMIN) {
            return true;
        }
        return role == UserRole.MANAGER && MANAGER_PERMISSIONS.contains(permission);
    }
}
