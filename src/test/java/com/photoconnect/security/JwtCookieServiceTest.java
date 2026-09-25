package com.photoconnect.security;

import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class JwtCookieServiceTest {

    private static final String SECRET = "UGhvdG9Db25uZWN0LUQwMS10ZXN0LW9ubHktc2VjcmV0LWtleS0yMDI2";

    @Test
    void issueUsesHttpOnlySameSitePathLifetimeAndConfiguredSecureFlag() {
        DefaultJwtCookieService service = cookieService(true);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/photoconnect");
        MockHttpServletResponse response = new MockHttpServletResponse();

        service.issue(request, response, user());

        String header = response.getHeader("Set-Cookie");
        assertNotNull(header);
        assertTrue(header.startsWith(JwtCookieService.COOKIE_NAME + "="));
        assertTrue(header.contains("Path=/photoconnect"));
        assertTrue(header.contains("Max-Age=1800"));
        assertTrue(header.contains("Secure"));
        assertTrue(header.contains("HttpOnly"));
        assertTrue(header.contains("SameSite=Lax"));
    }

    @Test
    void localCookieCanBeNonSecureAndLogoutExpiresIt() {
        DefaultJwtCookieService service = cookieService(false);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        service.clear(request, response);

        String header = response.getHeader("Set-Cookie");
        assertNotNull(header);
        assertTrue(header.contains("Path=/"));
        assertTrue(header.contains("Max-Age=0"));
        assertTrue(header.contains("HttpOnly"));
        assertFalse(header.contains("; Secure"));
    }

    private DefaultJwtCookieService cookieService(boolean secure) {
        return new DefaultJwtCookieService(new JwtTokenService(SECRET, Duration.ofMinutes(30)), secure);
    }

    private User user() {
        User user = new User();
        user.setId(5L);
        user.setRole(UserRole.CUSTOMER);
        return user;
    }
}
