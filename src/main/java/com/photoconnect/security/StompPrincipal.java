package com.photoconnect.security;

import com.photoconnect.entity.UserRole;

import java.security.Principal;
import java.util.Objects;

/**
 * Security principal representing an authenticated user over a STOMP WebSocket connection.
 */
public class StompPrincipal implements Principal {

    private final String name;
    private final Long userId;
    private final UserRole role;

    public StompPrincipal(String name, Long userId, UserRole role) {
        this.name = Objects.requireNonNull(name, "name is required");
        this.userId = Objects.requireNonNull(userId, "userId is required");
        this.role = Objects.requireNonNull(role, "role is required");
    }

    public StompPrincipal(Long userId, UserRole role) {
        this(String.valueOf(userId), userId, role);
    }

    @Override
    public String getName() {
        return name;
    }

    public Long getUserId() {
        return userId;
    }

    public UserRole getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StompPrincipal that = (StompPrincipal) o;
        return Objects.equals(userId, that.userId) && role == that.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, role);
    }
}
