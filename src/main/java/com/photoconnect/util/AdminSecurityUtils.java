package com.photoconnect.util;

import com.photoconnect.entity.UserRole;
import jakarta.servlet.http.HttpSession;

/**
 * Utility for verifying administrator authentication and authorization in HTTP sessions.
 */
public final class AdminSecurityUtils {

    private AdminSecurityUtils() {
    }

    /**
     * Checks whether the current session has an authenticated ADMIN role.
     */
    public static boolean isAdmin(HttpSession session) {
        return SessionSecurityUtils.hasRole(session, UserRole.ADMIN);
    }

    /**
     * Verifies that the session belongs to an authenticated administrator.
     * Returns a redirect target if unauthorized, or null if authorized.
     */
    public static String requireAdmin(HttpSession session) {
        return SessionSecurityUtils.requireRole(session, UserRole.ADMIN);
    }
}
