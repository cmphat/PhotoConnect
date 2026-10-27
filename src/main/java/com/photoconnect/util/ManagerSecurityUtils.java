package com.photoconnect.util;

import com.photoconnect.entity.UserRole;
import jakarta.servlet.http.HttpSession;

public final class ManagerSecurityUtils {

    private ManagerSecurityUtils() {
    }

    public static boolean isManager(HttpSession session) {
        return SessionSecurityUtils.hasRole(session, UserRole.MANAGER);
    }

    public static String requireManager(HttpSession session) {
        return SessionSecurityUtils.requireRole(session, UserRole.MANAGER);
    }
}
