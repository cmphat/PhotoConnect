package com.photoconnect.security;

import com.photoconnect.entity.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagerRoleAuthorizationTest {
    @Test
    void managerHasOperationalPermissionsButNotRoleOrSettingsManagement() {
        assertTrue(RolePermissionPolicy.hasPermission(UserRole.MANAGER, PlatformPermission.PHOTOGRAPHER_APPLICATION_REVIEW));
        assertTrue(RolePermissionPolicy.hasPermission(UserRole.MANAGER, PlatformPermission.BOOKING_DISPUTE_RESOLVE));
        assertTrue(RolePermissionPolicy.hasPermission(UserRole.MANAGER, PlatformPermission.SUPPORT_TICKET_MANAGE));
        assertFalse(RolePermissionPolicy.hasPermission(UserRole.MANAGER, PlatformPermission.ACCOUNT_ROLE_MANAGE));
        assertFalse(RolePermissionPolicy.hasPermission(UserRole.MANAGER, PlatformPermission.SYSTEM_SETTINGS_MANAGE));
    }

    @Test
    void customerAndPhotographerHaveNoManagerPermissions() {
        for (PlatformPermission permission : PlatformPermission.values()) {
            assertFalse(RolePermissionPolicy.hasPermission(UserRole.CUSTOMER, permission));
            assertFalse(RolePermissionPolicy.hasPermission(UserRole.PHOTOGRAPHER, permission));
        }
    }

    @Test
    void managerRouteRequiresManagerRoleAndAdminRouteRemainsAdminOnly() {
        assertEquals(UserRole.MANAGER, JwtAuthenticationFilter.requiredRole("/manager/reports"));
        assertEquals(UserRole.ADMIN, JwtAuthenticationFilter.requiredRole("/admin/users"));
    }
}
