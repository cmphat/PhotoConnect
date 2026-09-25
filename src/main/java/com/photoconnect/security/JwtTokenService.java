package com.photoconnect.security;

import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class JwtTokenService {

    private final SecretKey signingKey;
    private final Duration lifetime;
    private final Clock clock;

    public JwtTokenService(String base64Secret, Duration lifetime) {
        this(base64Secret, lifetime, Clock.systemUTC());
    }

    JwtTokenService(String base64Secret, Duration lifetime, Clock clock) {
        if (base64Secret == null || base64Secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET is required and must be Base64 encoded");
        }
        if (lifetime == null || lifetime.isZero() || lifetime.isNegative() || lifetime.compareTo(Duration.ofHours(24)) > 0) {
            throw new IllegalStateException("JWT expiration must be greater than zero and no more than 24 hours");
        }
        try {
            byte[] keyBytes = Decoders.BASE64.decode(base64Secret.trim());
            if (keyBytes.length < 32) {
                throw new IllegalStateException("JWT_SECRET must decode to at least 32 bytes");
            }
            this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new IllegalStateException("JWT_SECRET must be valid Base64 and decode to at least 32 bytes", ex);
        }
        this.lifetime = lifetime;
        this.clock = clock;
    }

    public String createToken(User user) {
        if (user == null || user.getId() == null || user.getRole() == null) {
            throw new IllegalArgumentException("A persisted user with a role is required");
        }
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(lifetime);
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .id(UUID.randomUUID().toString())
                .signWith(signingKey)
                .compact();
    }

    public JwtPrincipal parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .clock(() -> Date.from(clock.instant()))
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long userId = Long.valueOf(claims.getSubject());
            UserRole role = UserRole.valueOf(claims.get("role", String.class));
            Date issuedAt = claims.getIssuedAt();
            Date expiresAt = claims.getExpiration();
            String tokenId = claims.getId();
            if (userId <= 0 || issuedAt == null || expiresAt == null || tokenId == null || tokenId.isBlank()
                    || !expiresAt.toInstant().isAfter(clock.instant())) {
                throw new JwtValidationException();
            }
            return new JwtPrincipal(userId, role, issuedAt.toInstant(), expiresAt.toInstant(), tokenId);
        } catch (JwtValidationException ex) {
            throw ex;
        } catch (JwtException | IllegalArgumentException | NullPointerException ex) {
            throw new JwtValidationException(ex);
        }
    }

    public Duration getLifetime() {
        return lifetime;
    }
}
