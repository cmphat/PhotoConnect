package com.photoconnect.security;

import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CrossRoleAuthorizationSecurityTest {

    private static final String TEST_SECRET = "UGhvdG9Db25uZWN0LUQwMS10ZXN0LW9ubHktc2VjcmV0LWtleS0yMDI2";

    private JwtTokenService tokenService;
    private DefaultJwtCookieService cookieService;
    private UserRepository userRepository;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        tokenService = new JwtTokenService(TEST_SECRET, Duration.ofMinutes(60));
        cookieService = new DefaultJwtCookieService(tokenService, false);
        userRepository = mock(UserRepository.class);
        filter = new JwtAuthenticationFilter(tokenService, cookieService, userRepository);
    }

    private User createUser(Long id, UserRole role, UserStatus status) {
        User user = new User();
        user.setId(id);
        user.setEmail("user" + id + "@photoconnect.test");
        user.setFullName("Test User " + id);
        user.setRole(role);
        user.setStatus(status);
        return user;
    }

    private MockHttpServletRequest createAuthenticatedRequest(String method, String path, User user) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setCookies(new Cookie(JwtCookieService.COOKIE_NAME, tokenService.createToken(user)));
        return request;
    }

    private int executeRequest(MockHttpServletRequest request, MockHttpServletResponse response) throws Exception {
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        return response.getStatus();
    }

    @Nested
    @DisplayName("Guest (Unauthenticated) Access Boundary")
    class GuestAccessTests {

        @ParameterizedTest(name = "Guest accessing {0} should be redirected to /login")
        @ValueSource(strings = {
                "/customer/dashboard",
                "/customer/saved-photographers",
                "/photographer/dashboard",
                "/photographer/packages",
                "/photographer/analytics",
                "/admin/dashboard",
                "/admin/bookings",
                "/admin/users",
                "/bookings",
                "/become-photographer"
        })
        void guestAccessingProtectedPagesRedirectsToLogin(String path) throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
            MockHttpServletResponse response = new MockHttpServletResponse();

            int status = executeRequest(request, response);

            assertThat(status).isEqualTo(302);
            assertThat(response.getRedirectedUrl()).isEqualTo("/login");
        }

        @ParameterizedTest(name = "Guest accessing API {0} should receive 401 Unauthorized")
        @ValueSource(strings = {
                "/api/bookings",
                "/api/bookings/1/messages",
                "/api/me/profile"
        })
        void guestAccessingProtectedApisReceives401(String path) throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
            MockHttpServletResponse response = new MockHttpServletResponse();

            int status = executeRequest(request, response);

            assertThat(status).isEqualTo(401);
            assertThat(response.getContentAsString()).contains("Authentication required");
        }
    }

    @Nested
    @DisplayName("Customer Role Access Boundary")
    class CustomerRoleBoundaryTests {

        private User customer;

        @BeforeEach
        void initCustomer() {
            customer = createUser(100L, UserRole.CUSTOMER, UserStatus.ACTIVE);
            when(userRepository.findById(100L)).thenReturn(Optional.of(customer));
        }

        @ParameterizedTest(name = "Customer can access customer route: {0}")
        @ValueSource(strings = {
                "/customer/dashboard",
                "/customer/saved-photographers",
                "/bookings",
                "/become-photographer"
        })
        void customerAllowedInCustomerAreas(String path) throws Exception {
            MockHttpServletRequest request = createAuthenticatedRequest("GET", path, customer);
            MockHttpServletResponse response = new MockHttpServletResponse();

            int status = executeRequest(request, response);

            assertThat(status).isEqualTo(200);
            AuthenticatedUser currentUser = (AuthenticatedUser) request.getAttribute(CurrentUserContext.REQUEST_ATTRIBUTE);
            assertThat(currentUser).isNotNull();
            assertThat(currentUser.userId()).isEqualTo(100L);
            assertThat(currentUser.role()).isEqualTo(UserRole.CUSTOMER);
        }

        @ParameterizedTest(name = "Customer is forbidden (403) from photographer route: {0}")
        @ValueSource(strings = {
                "/photographer/dashboard",
                "/photographer/packages",
                "/photographer/analytics",
                "/photographer/bookings"
        })
        void customerRejectedFromPhotographerAreas(String path) throws Exception {
            MockHttpServletRequest request = createAuthenticatedRequest("GET", path, customer);
            MockHttpServletResponse response = new MockHttpServletResponse();

            int status = executeRequest(request, response);

            assertThat(status).isEqualTo(403);
        }

        @ParameterizedTest(name = "Customer is forbidden (403) from admin route: {0}")
        @ValueSource(strings = {
                "/admin/dashboard",
                "/admin/bookings",
                "/admin/users",
                "/admin/reports"
        })
        void customerRejectedFromAdminAreas(String path) throws Exception {
            MockHttpServletRequest request = createAuthenticatedRequest("GET", path, customer);
            MockHttpServletResponse response = new MockHttpServletResponse();

            int status = executeRequest(request, response);

            assertThat(status).isEqualTo(403);
        }
    }

    @Nested
    @DisplayName("Photographer Role Access Boundary")
    class PhotographerRoleBoundaryTests {

        private User photographer;

        @BeforeEach
        void initPhotographer() {
            photographer = createUser(200L, UserRole.PHOTOGRAPHER, UserStatus.ACTIVE);
            when(userRepository.findById(200L)).thenReturn(Optional.of(photographer));
        }

        @ParameterizedTest(name = "Photographer can access studio route: {0}")
        @ValueSource(strings = {
                "/photographer/dashboard",
                "/photographer/packages",
                "/photographer/analytics",
                "/photographer/bookings"
        })
        void photographerAllowedInStudioAreas(String path) throws Exception {
            MockHttpServletRequest request = createAuthenticatedRequest("GET", path, photographer);
            MockHttpServletResponse response = new MockHttpServletResponse();

            int status = executeRequest(request, response);

            assertThat(status).isEqualTo(200);
            AuthenticatedUser currentUser = (AuthenticatedUser) request.getAttribute(CurrentUserContext.REQUEST_ATTRIBUTE);
            assertThat(currentUser).isNotNull();
            assertThat(currentUser.userId()).isEqualTo(200L);
            assertThat(currentUser.role()).isEqualTo(UserRole.PHOTOGRAPHER);
        }

        @ParameterizedTest(name = "Photographer is forbidden (403) from customer route: {0}")
        @ValueSource(strings = {
                "/customer/dashboard",
                "/customer/saved-photographers",
                "/become-photographer"
        })
        void photographerRejectedFromCustomerAreas(String path) throws Exception {
            MockHttpServletRequest request = createAuthenticatedRequest("GET", path, photographer);
            MockHttpServletResponse response = new MockHttpServletResponse();

            int status = executeRequest(request, response);

            assertThat(status).isEqualTo(403);
        }

        @ParameterizedTest(name = "Photographer is forbidden (403) from admin route: {0}")
        @ValueSource(strings = {
                "/admin/dashboard",
                "/admin/bookings",
                "/admin/users",
                "/admin/reports"
        })
        void photographerRejectedFromAdminAreas(String path) throws Exception {
            MockHttpServletRequest request = createAuthenticatedRequest("GET", path, photographer);
            MockHttpServletResponse response = new MockHttpServletResponse();

            int status = executeRequest(request, response);

            assertThat(status).isEqualTo(403);
        }
    }

    @Nested
    @DisplayName("Admin Role Access Boundary")
    class AdminRoleBoundaryTests {

        private User admin;

        @BeforeEach
        void initAdmin() {
            admin = createUser(300L, UserRole.ADMIN, UserStatus.ACTIVE);
            when(userRepository.findById(300L)).thenReturn(Optional.of(admin));
        }

        @ParameterizedTest(name = "Admin can access administrative route: {0}")
        @ValueSource(strings = {
                "/admin/dashboard",
                "/admin/bookings",
                "/admin/users"
        })
        void adminAllowedInAdminAreas(String path) throws Exception {
            MockHttpServletRequest request = createAuthenticatedRequest("GET", path, admin);
            MockHttpServletResponse response = new MockHttpServletResponse();

            int status = executeRequest(request, response);

            assertThat(status).isEqualTo(200);
            AuthenticatedUser currentUser = (AuthenticatedUser) request.getAttribute(CurrentUserContext.REQUEST_ATTRIBUTE);
            assertThat(currentUser).isNotNull();
            assertThat(currentUser.userId()).isEqualTo(300L);
            assertThat(currentUser.role()).isEqualTo(UserRole.ADMIN);
        }

        @ParameterizedTest(name = "Admin is rejected from customer and photographer portals: {0}")
        @ValueSource(strings = {
                "/customer/dashboard",
                "/customer/saved-photographers",
                "/photographer/dashboard",
                "/photographer/packages",
                "/photographer/analytics"
        })
        void adminRejectedFromTenantPortals(String path) throws Exception {
            MockHttpServletRequest request = createAuthenticatedRequest("GET", path, admin);
            MockHttpServletResponse response = new MockHttpServletResponse();

            int status = executeRequest(request, response);

            assertThat(status).isEqualTo(403);
        }
    }

    @Nested
    @DisplayName("Account Status & Security Invariants")
    class AccountStatusInvariantsTests {

        @Test
        @DisplayName("Inactive or suspended accounts cannot authenticate even with valid JWT")
        void inactiveAccountIsRejectedAndTokenCleared() throws Exception {
            User inactiveUser = createUser(400L, UserRole.CUSTOMER, UserStatus.INACTIVE);
            when(userRepository.findById(400L)).thenReturn(Optional.of(inactiveUser));

            MockHttpServletRequest request = createAuthenticatedRequest("GET", "/customer/dashboard", inactiveUser);
            MockHttpServletResponse response = new MockHttpServletResponse();

            int status = executeRequest(request, response);

            assertThat(status).isEqualTo(302);
            assertThat(response.getRedirectedUrl()).isEqualTo("/login");
            assertThat(response.getHeaders("Set-Cookie"))
                    .anyMatch(cookie -> cookie.contains("Max-Age=0"));
        }

        @Test
        @DisplayName("Tampered role in JWT claim is caught against database authority")
        void roleMismatchBetweenJwtAndDbIsRejected() throws Exception {
            // DB has CUSTOMER, but malicious token claims ADMIN
            User dbUser = createUser(500L, UserRole.CUSTOMER, UserStatus.ACTIVE);
            when(userRepository.findById(500L)).thenReturn(Optional.of(dbUser));

            User maliciousTokenUser = createUser(500L, UserRole.ADMIN, UserStatus.ACTIVE);
            MockHttpServletRequest request = createAuthenticatedRequest("GET", "/admin/dashboard", maliciousTokenUser);
            MockHttpServletResponse response = new MockHttpServletResponse();

            int status = executeRequest(request, response);

            assertThat(status).isEqualTo(302);
            assertThat(response.getRedirectedUrl()).isEqualTo("/login");
        }
    }
}
