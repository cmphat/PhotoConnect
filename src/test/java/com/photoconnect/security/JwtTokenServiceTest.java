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
                        () -> new JwtTokenService("c2hvcnQ=", Duration.ofMinutes(30))),
                () -> assertThrows(IllegalStateException.class,
                        () -> new JwtTokenService(SECRET, Duration.ofDays(2)))
        );
    }

    private JwtTokenService serviceAt(Instant instant) {
        return new JwtTokenService(SECRET, Duration.ofMinutes(30), Clock.fixed(instant, ZoneOffset.UTC));
    }
}
