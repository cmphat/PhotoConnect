package com.photoconnect.security;

import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenServiceTest {

    private static final String SECRET = "UGhvdG9Db25uZWN0LUQwMS10ZXN0LW9ubHktc2VjcmV0LWtleS0yMDI2";
    private static final String OTHER_SECRET = "YW5vdGhlci1jb21wbGV0ZWx5LWRpZmZlcmVudC10ZXN0LW9ubHkta2V5LTIwMjY=";
    private static final Instant NOW = Instant.parse("2026-09-23T08:00:00Z");

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(42L);
        user.setRole(UserRole.PHOTOGRAPHER);
        user.setEmail("not-in-token@example.com");
        user.setPassword("not-in-token");
    }

    @Test
    void createsAndParsesMinimalValidatedClaims() {
        JwtTokenService service = serviceAt(NOW);

        JwtPrincipal principal = service.parseToken(service.createToken(user));

        assertEquals(42L, principal.userId());
        assertEquals(UserRole.PHOTOGRAPHER, principal.role());
        assertEquals(NOW, principal.issuedAt());
        assertEquals(NOW.plus(Duration.ofMinutes(30)), principal.expiresAt());
        assertNotNull(principal.tokenId());
        assertFalse(principal.tokenId().isBlank());
    }

    @Test
    void rejectsExpiredToken() {
        String token = serviceAt(NOW).createToken(user);
        JwtTokenService later = serviceAt(NOW.plus(Duration.ofMinutes(31)));

        assertThrows(JwtValidationException.class, () -> later.parseToken(token));
    }

    @Test
    void rejectsTokenSignedWithDifferentKey() {
        String token = serviceAt(NOW).createToken(user);
        JwtTokenService other = new JwtTokenService(OTHER_SECRET, Duration.ofMinutes(30),
                Clock.fixed(NOW, ZoneOffset.UTC));

        assertThrows(JwtValidationException.class, () -> other.parseToken(token));
    }

    @Test
    void rejectsMalformedAndTamperedTokens() {
        JwtTokenService service = serviceAt(NOW);
        String token = service.createToken(user);
        String[] parts = token.split("\\.");
        String signature = parts[2];
        String tampered = parts[0] + "." + parts[1] + "."
                + (signature.startsWith("a") ? "b" : "a") + signature.substring(1);

        assertAll(
                () -> assertThrows(JwtValidationException.class, () -> service.parseToken("not-a-jwt")),
                () -> assertThrows(JwtValidationException.class, () -> service.parseToken(tampered))
        );
    }

    @Test
    void refusesMissingWeakOrUnboundedConfiguration() {
        assertAll(
                () -> assertThrows(IllegalStateException.class,
                        () -> new JwtTokenService("", Duration.ofMinutes(30))),
                () -> assertThrows(IllegalStateException.class,
                        () -> new JwtTokenService("   ", Duration.ofMinutes(30))),
                () -> assertThrows(IllegalStateException.class,
                        () -> new JwtTokenService("c2hvcnQ=", Duration.ofMinutes(30))),
                () -> assertThrows(IllegalStateException.class,
                        () -> new JwtTokenService(SECRET, Duration.ofDays(2)))
        );
    }

    @Test
    void refusesCompromisedKnownSecrets() {
        assertAll(
                () -> assertThrows(IllegalStateException.class,
                        () -> new JwtTokenService("dGhpc2lzYXZlcnlzZWN1cmVqd3RzZWNyZXRmb3JwaG90b2Nvbm5lY3QyMDI2", Duration.ofMinutes(30))),
                () -> assertThrows(IllegalStateException.class,
                        () -> new JwtTokenService("c2VjdXJlLXBob3RvY29ubmVjdC1qd3Qtc2VjcmV0LWtleS0yMDI2LW11c3QtYmUtYXQtbGVhc3QtMjU2LWJpdHM=", Duration.ofMinutes(30))),
                // Regression: the historical key published in the V4 release-blocker report.
                () -> assertThrows(IllegalStateException.class,
                        () -> new JwtTokenService("dGhpc2lzYXZlcnlsb25nc2VjcmV0a2V5Zm9ycGhvdG9jb25uZWN0and0YXV0aGVudGljYXRpb24=", Duration.ofMinutes(30)))
        );
    }

    @Test
    void rejectsForgedTokenSignedWithCompromisedSecret() {
        // Suppose an attacker creates a token signed with the old compromised secret
        byte[] compromisedKeyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode("dGhpc2lzYXZlcnlzZWN1cmVqd3RzZWNyZXRmb3JwaG90b2Nvbm5lY3QyMDI2");
        javax.crypto.SecretKey attackerKey = io.jsonwebtoken.security.Keys.hmacShaKeyFor(compromisedKeyBytes);
        String forgedToken = io.jsonwebtoken.Jwts.builder()
                .subject("1")
                .claim("role", "ADMIN")
                .issuedAt(java.util.Date.from(NOW))
                .expiration(java.util.Date.from(NOW.plus(Duration.ofMinutes(30))))
                .id(java.util.UUID.randomUUID().toString())
                .signWith(attackerKey)
                .compact();

        // The secure server using SECRET must strictly reject the forged token
        JwtTokenService service = serviceAt(NOW);
        assertThrows(JwtValidationException.class, () -> service.parseToken(forgedToken));
    }

    private JwtTokenService serviceAt(Instant instant) {
        return new JwtTokenService(SECRET, Duration.ofMinutes(30), Clock.fixed(instant, ZoneOffset.UTC));
    }
}
