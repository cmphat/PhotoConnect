package com.photoconnect.security;

import com.photoconnect.config.GoogleOAuthProperties;
import com.photoconnect.entity.ExternalAuthProvider;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.exception.AccountDisabledException;
import com.photoconnect.oauth.ExternalAccountLinkRequiredException;
import com.photoconnect.oauth.VerifiedExternalIdentity;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.service.ExternalAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GoogleOidcAuthenticationSuccessHandlerTest {

    private ExternalAuthService externalAuthService;
    private JwtCookieService jwtCookieService;
    private GoogleOidcAuthenticationSuccessHandler handler;
    private User customer;

    @BeforeEach
    void setUp() {
        externalAuthService = mock(ExternalAuthService.class);
        jwtCookieService = mock(JwtCookieService.class);
        PhotographerProfileRepository profiles = mock(PhotographerProfileRepository.class);
        GoogleOAuthProperties properties = new GoogleOAuthProperties();
        properties.setClientId("google-client-id.apps.googleusercontent.com");
        properties.setClientSecret("test-secret");
        handler = new GoogleOidcAuthenticationSuccessHandler(
                properties, externalAuthService, jwtCookieService, profiles);
        customer = new User();
        customer.setId(44L);
        customer.setEmail("person@example.com");
        customer.setFullName("Person Name");
        customer.setRole(UserRole.CUSTOMER);
        customer.setStatus(UserStatus.ACTIVE);
    }

    @Test
    void verifiedNewGoogleIdentityCreatesCustomerAndIssuesApplicationJwt() throws Exception {
        when(externalAuthService.findLinkedUser(any())).thenReturn(Optional.empty());
        when(externalAuthService.createCustomer(any())).thenReturn(customer);
        MockHttpServletRequest request = request();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, authentication(claims(true)));

        assertEquals("/customer/dashboard", response.getRedirectedUrl());
        assertEquals(44L, request.getSession().getAttribute("userId"));
        assertEquals("CUSTOMER", request.getSession().getAttribute("userRole"));
        verify(externalAuthService).createCustomer(argThat(identity ->
                identity.provider() == ExternalAuthProvider.GOOGLE
                        && identity.subject().equals("google-subject")
                        && identity.email().equals("person@example.com")));
        verify(jwtCookieService).issue(request, response, customer);
    }

    @Test
    void existingLinkedAccountLogsInWithoutCreatingAnotherUser() throws Exception {
        when(externalAuthService.findLinkedUser(any())).thenReturn(Optional.of(customer));
        MockHttpServletRequest request = request();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, authentication(claims(true)));

        assertEquals("/customer/dashboard", response.getRedirectedUrl());
        verify(externalAuthService, never()).createCustomer(any());
        verify(jwtCookieService).issue(request, response, customer);
    }

    @Test
    void unverifiedEmailIsRejectedBeforeLocalAccountLookup() throws Exception {
        MockHttpServletResponse response = invoke(claims(false));
        assertEquals("/login?oauth=invalid-identity", response.getRedirectedUrl());
        verifyNoInteractions(externalAuthService, jwtCookieService);
    }

    @Test
    void wrongIssuerIsRejected() throws Exception {
        Map<String, Object> claims = claims(true);
        claims.put("iss", "https://attacker.example");
        MockHttpServletResponse response = invoke(claims);
        assertEquals("/login?oauth=invalid-identity", response.getRedirectedUrl());
        verifyNoInteractions(externalAuthService, jwtCookieService);
    }

    @Test
    void wrongAudienceIsRejected() throws Exception {
        Map<String, Object> claims = claims(true);
        claims.put("aud", List.of("different-client"));
        MockHttpServletResponse response = invoke(claims);
        assertEquals("/login?oauth=invalid-identity", response.getRedirectedUrl());
        verifyNoInteractions(externalAuthService, jwtCookieService);
    }

    @Test
    void expiredIdTokenIsRejected() throws Exception {
        Map<String, Object> claims = claims(true);
        Instant issuedAt = Instant.now().minusSeconds(600);
        Instant expiresAt = Instant.now().minusSeconds(60);
        claims.put("iat", issuedAt);
        claims.put("exp", expiresAt);
        OidcIdToken idToken = new OidcIdToken("expired-token", issuedAt, expiresAt, claims);
        DefaultOidcUser principal = new DefaultOidcUser(
                List.of(new SimpleGrantedAuthority("OIDC_USER")), idToken);
        OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
                principal, principal.getAuthorities(), "google");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request(), response, authentication);

        assertEquals("/login?oauth=invalid-identity", response.getRedirectedUrl());
        verifyNoInteractions(externalAuthService, jwtCookieService);
    }

    @Test
    void unlinkedSameEmailCannotTakeOverLocalAccount() throws Exception {
        when(externalAuthService.findLinkedUser(any())).thenThrow(new ExternalAccountLinkRequiredException());
        MockHttpServletResponse response = invoke(claims(true));
        assertEquals("/login?oauth=unlinked-email", response.getRedirectedUrl());
        verify(jwtCookieService, never()).issue(any(), any(), any());
    }

    @Test
    void bannedLinkedAccountCannotReceiveApplicationJwt() throws Exception {
        when(externalAuthService.findLinkedUser(any()))
                .thenThrow(new AccountDisabledException("inactive"));
        MockHttpServletResponse response = invoke(claims(true));
        assertEquals("/login?oauth=account-disabled", response.getRedirectedUrl());
        verify(jwtCookieService, never()).issue(any(), any(), any());
    }

    private MockHttpServletResponse invoke(Map<String, Object> claims) throws Exception {
        MockHttpServletRequest request = request();
        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(request, response, authentication(claims));
        return response;
    }

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertNotNull(request.getSession(true));
        return request;
    }

    private OAuth2AuthenticationToken authentication(Map<String, Object> claims) {
        Instant issuedAt = Instant.now().minusSeconds(30);
        Instant expiresAt = Instant.now().plusSeconds(300);
        OidcIdToken idToken = new OidcIdToken("signed-token-not-used-by-handler", issuedAt, expiresAt, claims);
        DefaultOidcUser principal = new DefaultOidcUser(
                List.of(new SimpleGrantedAuthority("OIDC_USER")), idToken);
        return new OAuth2AuthenticationToken(principal, principal.getAuthorities(), "google");
    }

    private Map<String, Object> claims(boolean emailVerified) {
        return new java.util.HashMap<>(Map.of(
                "iss", "https://accounts.google.com",
                "sub", "google-subject",
                "aud", List.of("google-client-id.apps.googleusercontent.com"),
                "iat", Instant.now().minusSeconds(30),
                "exp", Instant.now().plusSeconds(300),
                "email", "Person@Example.com",
                "email_verified", emailVerified,
                "name", "Person Name"
        ));
    }
}
