package com.photoconnect.security;

import com.photoconnect.entity.UserRole;

/** Identity established only after a signed JWT and persisted account are validated. */
public record AuthenticatedUser(Long userId, UserRole role, String fullName) {
}
