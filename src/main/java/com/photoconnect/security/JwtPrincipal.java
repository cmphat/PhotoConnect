package com.photoconnect.security;

import com.photoconnect.entity.UserRole;

import java.time.Instant;

/** Minimal validated claims used internally by the authentication filter. */
public record JwtPrincipal(Long userId, UserRole role, Instant issuedAt, Instant expiresAt, String tokenId) {
}
