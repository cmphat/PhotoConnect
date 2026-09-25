package com.photoconnect.util;

import com.photoconnect.entity.UserRole;
import jakarta.servlet.http.HttpSession;

/**
 * Compatibility access for legacy controllers/JSPs. TASK-D01's JWT filter is the
 * sole writer of these identity values on normal requests; new security-sensitive
 * code should use CurrentUserContext instead.
 */
public final class SessionSecurityUtils {

    private SessionSecurityUtils() {
    }

    public static Long userId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute("userId");
        return value instanceof Long id ? id : null;
    }

    public static boolean hasRole(HttpSession session, UserRole requiredRole) {
        if (requiredRole == null || userId(session) == null) {
            return false;
        }
        Object role = session.getAttribute("userRole");
        return role != null && requiredRole.name().equals(role.toString());
    }

    /** Returns null when authorized, otherwise a safe MVC redirect. */
    public static String requireRole(HttpSession session, UserRole requiredRole) {
        if (userId(session) == null) {
            return "redirect:/login";
        }
        return hasRole(session, requiredRole) ? null : "redirect:/";
    }
}
