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
        if (session == null) {
            return false;
        }
        Object role = session.getAttribute("userRole");
        return role != null && UserRole.ADMIN.name().equals(role.toString());
    }

    /**
     * Verifies that the session belongs to an authenticated administrator.
     * Returns a redirect target if unauthorized, or null if authorized.
     */
    public static String requireAdmin(HttpSession session) {
        if (session == null) {
            return "redirect:/login";
        }
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        if (!isAdmin(session)) {
            return "redirect:/"; // Logged in but not an administrator
        }
        return null;
    }
}
