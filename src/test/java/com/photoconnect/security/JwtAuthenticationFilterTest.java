package com.photoconnect.security;

import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "UGhvdG9Db25uZWN0LUQwMS10ZXN0LW9ubHktc2VjcmV0LWtleS0yMDI2";

    private JwtTokenService tokenService;
    private DefaultJwtCookieService cookieService;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        tokenService = new JwtTokenService(SECRET, Duration.ofMinutes(30));
        cookieService = new DefaultJwtCookieService(tokenService, false);
        userRepository = mock(UserRepository.class);
    }

    @Test
    void customerTokenEstablishesContextAndAllowsCustomerArea() throws Exception {
        User user = user(10L, UserRole.CUSTOMER, UserStatus.ACTIVE);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        MockHttpServletRequest request = authenticatedRequest("GET", "/customer/dashboard", user);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter().doFilter(request, response, chain);

        assertNotNull(chain.getRequest());
        AuthenticatedUser current = (AuthenticatedUser) request.getAttribute(CurrentUserContext.REQUEST_ATTRIBUTE);
        assertEquals(10L, current.userId());
        assertEquals("CUSTOMER", request.getSession().getAttribute("userRole"));
    }

    @Test
    void roleBoundariesRejectCustomerFromStudioAndAdmin() throws Exception {
        assertEquals(403, filterStatus(UserRole.CUSTOMER, "/photographer/dashboard"));
        assertEquals(403, filterStatus(UserRole.CUSTOMER, "/admin/dashboard"));
        assertEquals(403, filterStatus(UserRole.PHOTOGRAPHER, "/customer/dashboard"));
        assertEquals(403, filterStatus(UserRole.PHOTOGRAPHER, "/admin/dashboard"));
        assertEquals(200, filterStatus(UserRole.PHOTOGRAPHER, "/photographer/dashboard"));
        assertEquals(200, filterStatus(UserRole.ADMIN, "/admin/dashboard"));
    }

    @Test
    void guestAndInactiveAccountCannotUseProtectedRoutes() throws Exception {
        MockHttpServletRequest guest = new MockHttpServletRequest("GET", "/bookings");
        MockHttpServletResponse guestResponse = new MockHttpServletResponse();
        filter().doFilter(guest, guestResponse, new MockFilterChain());
        assertEquals(302, guestResponse.getStatus());
        assertEquals("/login", guestResponse.getRedirectedUrl());

        User inactive = user(12L, UserRole.CUSTOMER, UserStatus.INACTIVE);
        when(userRepository.findById(12L)).thenReturn(Optional.of(inactive));
        MockHttpServletRequest request = authenticatedRequest("GET", "/customer/dashboard", inactive);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter().doFilter(request, response, new MockFilterChain());
        assertEquals(302, response.getStatus());
        assertTrue(response.getHeaders("Set-Cookie").stream().anyMatch(value -> value.contains("Max-Age=0")));
    }

    @Test
    void apiAndWebSocketGuestsReceive401WithoutTokenInUrl() throws Exception {
        for (String path : new String[]{"/api/bookings/1/messages", "/ws/info", "/ws-raw"}) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter().doFilter(request, response, new MockFilterChain());
            assertEquals(401, response.getStatus(), path);
        }
    }

    @Test
    void explicitGoogleLinkRouteRequiresApplicationJwt() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/google/link");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter().doFilter(request, response, new MockFilterChain());
        assertEquals(302, response.getStatus());
        assertEquals("/login", response.getRedirectedUrl());
    }

    @Test
    void conflictingSessionIdentityIsRejectedAndCleared() throws Exception {
        User user = user(10L, UserRole.CUSTOMER, UserStatus.ACTIVE);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        MockHttpServletRequest request = authenticatedRequest("GET", "/customer/dashboard", user);
        request.getSession().setAttribute("userId", 999L);
        request.getSession().setAttribute("userRole", UserRole.ADMIN.name());
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter().doFilter(request, response, new MockFilterChain());

        assertEquals(302, response.getStatus());
        assertNull(request.getSession().getAttribute("userId"));
        assertTrue(response.getHeaders("Set-Cookie").stream().anyMatch(value -> value.contains("Max-Age=0")));
    }

    private int filterStatus(UserRole role, String path) throws Exception {
        long id = role.ordinal() + 100L;
        User user = user(id, role, UserStatus.ACTIVE);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        MockHttpServletRequest request = authenticatedRequest("GET", path, user);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter().doFilter(request, response, new MockFilterChain());
        return response.getStatus();
    }

    private MockHttpServletRequest authenticatedRequest(String method, String path, User user) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setCookies(new Cookie(JwtCookieService.COOKIE_NAME, tokenService.createToken(user)));
        return request;
    }

    private JwtAuthenticationFilter filter() {
        return new JwtAuthenticationFilter(tokenService, cookieService, userRepository);
    }

    private User user(Long id, UserRole role, UserStatus status) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setStatus(status);
        user.setEmail("user" + id + "@example.com");
        user.setFullName("User " + id);
        return user;
    }
}
